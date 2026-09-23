package bd.edu.seu.dhakatesla.service.impl;

import bd.edu.seu.dhakatesla.exception.InvalidStateTransitionException;
import bd.edu.seu.dhakatesla.exception.ResourceNotFoundException;
import bd.edu.seu.dhakatesla.model.*;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.RideRequestRepository;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.repository.VehicleRepository;
import bd.edu.seu.dhakatesla.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final VehicleRepository vehicleRepository;
    private final RidePoolRepository ridePoolRepository;
    private final RideRequestRepository rideRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Vehicle toggleDriverOnline(Long driverId, boolean online) {
        Vehicle vehicle = vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for driver id: " + driverId));

        if (!online) {
            vehicle.setStatus(VehicleStatus.OFFLINE);
        } else {
            vehicle.setStatus(VehicleStatus.ONLINE);
        }
        return vehicleRepository.save(vehicle);
    }

    @Override
    public RidePool getActivePoolForDriver(Long driverId) {
        return ridePoolRepository.findFirstByVehicleDriverIdAndStatusIn(
                driverId,
                Arrays.asList(PoolStatus.OPEN, PoolStatus.FULL, PoolStatus.IN_PROGRESS)
        ).orElse(null);
    }

    @Override
    public List<RideRequest> getPassengersInActivePool(Long driverId) {
        RidePool pool = getActivePoolForDriver(driverId);
        if (pool == null) {
            return Collections.emptyList();
        }
        return rideRequestRepository.findByPoolId(pool.getId());
    }

    @Override
    @Transactional
    public RidePool advancePoolStage(Long poolId, PoolStatus targetStatus) {
        RidePool pool = ridePoolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + poolId));

        List<RideRequest> passengers = rideRequestRepository.findByPoolId(pool.getId());

        if (targetStatus == PoolStatus.IN_PROGRESS) {
            pool.setStatus(PoolStatus.IN_PROGRESS);
            for (RideRequest r : passengers) {
                if (r.getStatus() == RideStatus.MATCHED || r.getStatus() == RideStatus.DRIVER_ARRIVED) {
                    r.setStatus(RideStatus.STARTED);
                    rideRequestRepository.save(r);
                }
            }
        } else if (targetStatus == PoolStatus.COMPLETED) {
            pool.setStatus(PoolStatus.COMPLETED);
            BigDecimal totalFareEarned = BigDecimal.ZERO;

            for (RideRequest r : passengers) {
                if (r.getStatus() == RideStatus.STARTED || r.getStatus() == RideStatus.DRIVER_ARRIVED || r.getStatus() == RideStatus.MATCHED) {
                    r.setStatus(RideStatus.COMPLETED);
                    r.setCompletedAt(LocalDateTime.now());
                    totalFareEarned = totalFareEarned.add(r.getTotalFare());
                    rideRequestRepository.save(r);
                }
            }

            Vehicle vehicle = pool.getVehicle();
            vehicle.setStatus(VehicleStatus.ONLINE);
            vehicleRepository.save(vehicle);

            User driver = vehicle.getDriver();
            driver.setWalletBalance(driver.getWalletBalance().add(totalFareEarned));
            userRepository.save(driver);
        } else {
            throw new InvalidStateTransitionException("Unsupported pool target status: " + targetStatus);
        }

        return ridePoolRepository.save(pool);
    }

    @Override
    public Vehicle getDriverVehicle(Long driverId) {
        return vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("No vehicle registered for driver id: " + driverId));
    }
}
