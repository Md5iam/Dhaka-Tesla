package bd.edu.seu.dhakatesla;

import bd.edu.seu.dhakatesla.exception.CapacityExceededException;
import bd.edu.seu.dhakatesla.model.PoolStatus;
import bd.edu.seu.dhakatesla.model.RidePool;
import bd.edu.seu.dhakatesla.model.Vehicle;
import bd.edu.seu.dhakatesla.model.VehicleStatus;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.VehicleRepository;
import bd.edu.seu.dhakatesla.repository.ZoneRepository;
import bd.edu.seu.dhakatesla.service.impl.PoolMatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CapacityEnforcementTest {

    @Mock
    private RidePoolRepository ridePoolRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ZoneRepository zoneRepository;

    private PoolMatchingServiceImpl poolMatchingService;

    @BeforeEach
    public void setup() {
        poolMatchingService = new PoolMatchingServiceImpl(ridePoolRepository, vehicleRepository, zoneRepository);
    }

    @Test
    public void testCapacityExceededThrowsException() {
        Vehicle bullet = Vehicle.builder()
                .id(1L)
                .modelName("Bullet")
                .licensePlate("DHAKA-1122")
                .capacity(3)
                .status(VehicleStatus.ONLINE)
                .build();

        RidePool openPool = RidePool.builder()
                .id(100L)
                .vehicle(bullet)
                .startZone("Banani")
                .destinationCorridor("Corridor-North-East")
                .status(PoolStatus.OPEN)
                .totalCapacity(3)
                .occupiedSeats(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(ridePoolRepository.findByStatusAndStartZone(PoolStatus.OPEN, "Banani"))
                .thenReturn(List.of(openPool));
        when(ridePoolRepository.findByIdWithLock(100L))
                .thenReturn(Optional.of(openPool));
        when(vehicleRepository.findByStatus(VehicleStatus.ONLINE))
                .thenReturn(Collections.emptyList());

        assertThrows(CapacityExceededException.class, () -> {
            poolMatchingService.findOrCreateMatchingPool("Banani", "Mohakhali", 2);
        });
    }

    @Test
    public void testFinalSeatFillsCapacityAndSetsFull() {
        Vehicle bullet = Vehicle.builder()
                .id(1L)
                .modelName("Bullet")
                .licensePlate("DHAKA-1122")
                .capacity(3)
                .status(VehicleStatus.ONLINE)
                .build();

        RidePool openPool = RidePool.builder()
                .id(100L)
                .vehicle(bullet)
                .startZone("Banani")
                .destinationCorridor("Corridor-North-East")
                .status(PoolStatus.OPEN)
                .totalCapacity(3)
                .occupiedSeats(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(ridePoolRepository.findByStatusAndStartZone(PoolStatus.OPEN, "Banani"))
                .thenReturn(List.of(openPool));
        when(ridePoolRepository.findByIdWithLock(100L))
                .thenReturn(Optional.of(openPool));
        when(ridePoolRepository.save(any(RidePool.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RidePool matchedPool = poolMatchingService.findOrCreateMatchingPool("Banani", "Mohakhali", 1);

        assertEquals(3, matchedPool.getOccupiedSeats());
        assertEquals(PoolStatus.FULL, matchedPool.getStatus());
    }
}
