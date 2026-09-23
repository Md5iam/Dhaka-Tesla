package bd.edu.seu.dhakatesla.service;

import bd.edu.seu.dhakatesla.model.RidePool;

public interface PoolMatchingService {
    RidePool findOrCreateMatchingPool(String pickupZone, String destinationZone, int requestedSeats);
    boolean isRouteCompatible(String poolStartZone, String poolCorridor, String pickupZone, String destinationZone);
    String resolveCorridor(String zoneName);
}
