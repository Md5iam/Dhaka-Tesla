package bd.edu.seu.dhakatesla.service.impl;

import bd.edu.seu.dhakatesla.dto.FareEstimateDTO;
import bd.edu.seu.dhakatesla.dto.RideBookingRequestDTO;
import bd.edu.seu.dhakatesla.exception.InvalidStateTransitionException;
import bd.edu.seu.dhakatesla.exception.ResourceNotFoundException;
import bd.edu.seu.dhakatesla.exception.ValidationException;
import bd.edu.seu.dhakatesla.model.*;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.RideRequestRepository;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.service.FareCalculationService;
import bd.edu.seu.dhakatesla.service.PoolMatchingService;
import bd.edu.seu.dhakatesla.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRequestRepository rideRequestRepository;
    private final RidePoolRepository ridePoolRepository;
    private final UserRepository userRepository;
    private final FareCalculationService fareCalculationService;
    private final PoolMatchingService poolMatchingService;

    @Override
    @Transactional
    public RideRequest bookRide(RideBookingRequestDTO requestDTO) {
        if (requestDTO.getPickupZone().trim().equalsIgnoreCase(requestDTO.getDestinationZone().trim())) {
            throw new ValidationException("Pickup and destination zones cannot be the same");
        }

        User passenger = userRepository.findById(requestDTO.getPassengerId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + requestDTO.getPassengerId()));

        if (passenger.getRole() != UserRole.PASSENGER) {
            throw new ValidationException("Only registered passengers can book a ride");
        }

        Optional<RideRequest> activeRide = rideRequestRepository.findFirstByPassengerIdAndStatusInOrderByRequestedAtDesc(
                passenger.getId(),
                Arrays.asList(RideStatus.REQUESTED, RideStatus.MATCHED, RideStatus.DRIVER_ARRIVED, RideStatus.STARTED)
        );
        if (activeRide.isPresent()) {
            throw new ValidationException("Passenger already has an active ride in progress");
        }

        FareEstimateDTO fareEstimate = fareCalculationService.estimateFare(
                requestDTO.getPickupZone(),
                requestDTO.getDestinationZone(),
                requestDTO.getRequestedSeats()
        );

        if (passenger.getWalletBalance().compareTo(fareEstimate.getPooledFare()) < 0) {
            throw new ValidationException("Insufficient TeslaPay wallet balance. Required: "
                    + fareEstimate.getPooledFare() + " BDT, Current: " + passenger.getWalletBalance() + " BDT");
        }

        RidePool pool = poolMatchingService.findOrCreateMatchingPool(
                requestDTO.getPickupZone(),
                requestDTO.getDestinationZone(),
                requestDTO.getRequestedSeats()
        );

        passenger.setWalletBalance(passenger.getWalletBalance().subtract(fareEstimate.getPooledFare()));
        userRepository.save(passenger);

        RideRequest rideRequest = RideRequest.builder()
                .passenger(passenger)
                .pool(pool)
                .pickupZone(requestDTO.getPickupZone())
                .destinationZone(requestDTO.getDestinationZone())
                .requestedSeats(requestDTO.getRequestedSeats())
                .distanceKm(fareEstimate.getDistanceKm())
                .baseFare(fareEstimate.getBaseFare())
                .distanceCharge(fareEstimate.getDistanceCharge())
                .poolDiscount(fareEstimate.getPoolDiscount())
                .totalFare(fareEstimate.getPooledFare())
                .status(RideStatus.MATCHED)
                .requestedAt(LocalDateTime.now())
                .build();

        return rideRequestRepository.save(rideRequest);
    }

    @Override
    @Transactional
    public RideRequest updateRideStatus(Long rideId, RideStatus newStatus) {
        RideRequest ride = getRideById(rideId);
        validateStateTransition(ride.getStatus(), newStatus);

        ride.setStatus(newStatus);
        if (newStatus == RideStatus.COMPLETED) {
            ride.setCompletedAt(LocalDateTime.now());
        }

        return rideRequestRepository.save(ride);
    }

    @Override
    @Transactional
    public RideRequest cancelRide(Long rideId) {
        RideRequest ride = getRideById(rideId);

        if (ride.getStatus() == RideStatus.STARTED || ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidStateTransitionException("Cannot cancel ride that has already started or completed");
        }
        if (ride.getStatus() == RideStatus.CANCELLED) {
            return ride;
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCompletedAt(LocalDateTime.now());

        User passenger = ride.getPassenger();
        passenger.setWalletBalance(passenger.getWalletBalance().add(ride.getTotalFare()));
        userRepository.save(passenger);

        RidePool pool = ride.getPool();
        if (pool != null) {
            int newOccupied = Math.max(0, pool.getOccupiedSeats() - ride.getRequestedSeats());
            pool.setOccupiedSeats(newOccupied);
            if (pool.getStatus() == PoolStatus.FULL && newOccupied < pool.getTotalCapacity()) {
                pool.setStatus(PoolStatus.OPEN);
            }
            ridePoolRepository.save(pool);
        }

        return rideRequestRepository.save(ride);
    }

    @Override
    public RideRequest getRideById(Long rideId) {
        return rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));
    }

    @Override
    public List<RideRequest> getPassengerRides(Long passengerId) {
        return rideRequestRepository.findByPassengerIdOrderByRequestedAtDesc(passengerId);
    }

    @Override
    public RideRequest getActivePassengerRide(Long passengerId) {
        return rideRequestRepository.findFirstByPassengerIdAndStatusInOrderByRequestedAtDesc(
                passengerId,
                Arrays.asList(RideStatus.REQUESTED, RideStatus.MATCHED, RideStatus.DRIVER_ARRIVED, RideStatus.STARTED)
        ).orElse(null);
    }

    private void validateStateTransition(RideStatus current, RideStatus target) {
        if (current == target) {
            return;
        }
        if (current == RideStatus.COMPLETED || current == RideStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Cannot transition from final status " + current);
        }
        if (current == RideStatus.MATCHED && target != RideStatus.DRIVER_ARRIVED && target != RideStatus.CANCELLED) {
            throw new InvalidStateTransitionException("MATCHED ride must transition to DRIVER_ARRIVED or CANCELLED");
        }
        if (current == RideStatus.DRIVER_ARRIVED && target != RideStatus.STARTED && target != RideStatus.CANCELLED) {
            throw new InvalidStateTransitionException("DRIVER_ARRIVED ride must transition to STARTED or CANCELLED");
        }
        if (current == RideStatus.STARTED && target != RideStatus.COMPLETED) {
            throw new InvalidStateTransitionException("STARTED ride can only transition to COMPLETED");
        }
    }
}
