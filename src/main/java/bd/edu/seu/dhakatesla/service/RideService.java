package bd.edu.seu.dhakatesla.service;

import bd.edu.seu.dhakatesla.dto.RideBookingRequestDTO;
import bd.edu.seu.dhakatesla.model.RideRequest;
import bd.edu.seu.dhakatesla.model.RideStatus;

import java.util.List;

public interface RideService {
    RideRequest bookRide(RideBookingRequestDTO requestDTO);
    RideRequest updateRideStatus(Long rideId, RideStatus newStatus);
    RideRequest cancelRide(Long rideId);
    RideRequest getRideById(Long rideId);
    List<RideRequest> getPassengerRides(Long passengerId);
    RideRequest getActivePassengerRide(Long passengerId);
}
