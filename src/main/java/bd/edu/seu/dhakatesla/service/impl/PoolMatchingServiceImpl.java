package bd.edu.seu.dhakatesla.service.impl;

import bd.edu.seu.dhakatesla.exception.CapacityExceededException;
import bd.edu.seu.dhakatesla.model.*;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.VehicleRepository;
import bd.edu.seu.dhakatesla.repository.ZoneRepository;
import bd.edu.seu.dhakatesla.service.PoolMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PoolMatchingServiceImpl implements PoolMatchingService {

    private final RidePoolRepository ridePoolRepository;
    private final VehicleRepository vehicleRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional
    public RidePool findOrCreateMatchingPool(String pickupZone, String destinationZone, int requestedSeats) {
        String targetCorridor = resolveCorridor(destinationZone);

        List<RidePool> openPools = ridePoolRepository.findByStatusAndStartZone(PoolStatus.OPEN, pickupZone);
        for (RidePool poolSummary : openPools) {
            RidePool pool = ridePoolRepository.findByIdWithLock(poolSummary.getId())
                    .orElse(poolSummary);

            if (pool.getStatus() == PoolStatus.OPEN
                    && isRouteCompatible(pool.getStartZone(), pool.getDestinationCorridor(), pickupZone, destinationZone)) {

                if (pool.getOccupiedSeats() + requestedSeats <= pool.getTotalCapacity()) {
                    pool.setOccupiedSeats(pool.getOccupiedSeats() + requestedSeats);
                    if (pool.getOccupiedSeats() >= pool.getTotalCapacity()) {
                        pool.setStatus(PoolStatus.FULL);
                    }
                    return ridePoolRepository.save(pool);
                }
            }
        }

        List<Vehicle> availableVehicles = vehicleRepository.findByStatus(VehicleStatus.ONLINE);
        for (Vehicle vehicle : availableVehicles) {
            Optional<RidePool> activePool = ridePoolRepository.findByVehicleAndStatusIn(
                    vehicle,
                    Arrays.asList(PoolStatus.OPEN, PoolStatus.FULL, PoolStatus.IN_PROGRESS)
            );

            if (activePool.isEmpty()) {
                if (requestedSeats > vehicle.getCapacity()) {
                    throw new CapacityExceededException("Requested seats exceed vehicle capacity of " + vehicle.getCapacity());
                }

                RidePool newPool = RidePool.builder()
                        .vehicle(vehicle)
                        .status(requestedSeats >= vehicle.getCapacity() ? PoolStatus.FULL : PoolStatus.OPEN)
                        .totalCapacity(vehicle.getCapacity())
                        .occupiedSeats(requestedSeats)
                        .startZone(pickupZone)
                        .destinationCorridor(targetCorridor)
                        .createdAt(LocalDateTime.now())
                        .build();

                vehicle.setStatus(VehicleStatus.ON_TRIP);
                vehicleRepository.save(vehicle);

                return ridePoolRepository.save(newPool);
            }
        }

        throw new CapacityExceededException("No available Tesla seats for route from " + pickupZone + " to " + destinationZone);
    }

    @Override
    public boolean isRouteCompatible(String poolStartZone, String poolCorridor, String pickupZone, String destinationZone) {
        if (!poolStartZone.equalsIgnoreCase(pickupZone)) {
            return false;
        }

        String targetCorridor = resolveCorridor(destinationZone);
        return poolCorridor.equalsIgnoreCase(targetCorridor);
    }

    @Override
    public String resolveCorridor(String zoneName) {
        Optional<Zone> zoneOpt = zoneRepository.findByName(zoneName);
        if (zoneOpt.isPresent()) {
            return zoneOpt.get().getCorridorGroup();
        }

        if (zoneName.equalsIgnoreCase("Mohakhali") || zoneName.equalsIgnoreCase("Gulshan 1") || zoneName.equalsIgnoreCase("Gulshan 2")) {
            return "Corridor-North-East";
        }
        if (zoneName.equalsIgnoreCase("Dhanmondi") || zoneName.equalsIgnoreCase("Farmgate")) {
            return "Corridor-Central-West";
        }
        return "Corridor-North";
    }
}
