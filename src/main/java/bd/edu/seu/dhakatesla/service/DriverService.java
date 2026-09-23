package bd.edu.seu.dhakatesla.service;

import bd.edu.seu.dhakatesla.model.PoolStatus;
import bd.edu.seu.dhakatesla.model.RidePool;
import bd.edu.seu.dhakatesla.model.RideRequest;
import bd.edu.seu.dhakatesla.model.Vehicle;

import java.util.List;

public interface DriverService {
    Vehicle toggleDriverOnline(Long driverId, boolean online);
    RidePool getActivePoolForDriver(Long driverId);
    List<RideRequest> getPassengersInActivePool(Long driverId);
    RidePool advancePoolStage(Long poolId, PoolStatus targetStatus);
    Vehicle getDriverVehicle(Long driverId);
}
