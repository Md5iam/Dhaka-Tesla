package bd.edu.seu.dhakatesla;

import bd.edu.seu.dhakatesla.exception.InvalidStateTransitionException;
import bd.edu.seu.dhakatesla.model.*;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.RideRequestRepository;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.service.FareCalculationService;
import bd.edu.seu.dhakatesla.service.PoolMatchingService;
import bd.edu.seu.dhakatesla.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StateTransitionTest {

    @Mock
    private RideRequestRepository rideRequestRepository;

    @Mock
    private RidePoolRepository ridePoolRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FareCalculationService fareCalculationService;

    @Mock
    private PoolMatchingService poolMatchingService;

    private RideServiceImpl rideService;

    @BeforeEach
    public void setup() {
        rideService = new RideServiceImpl(
                rideRequestRepository,
                ridePoolRepository,
                userRepository,
                fareCalculationService,
                poolMatchingService
        );
    }

    @Test
    public void testInvalidJumpFromMatchedToCompletedThrowsException() {
        RideRequest ride = RideRequest.builder()
                .id(1L)
                .status(RideStatus.MATCHED)
                .build();

        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStateTransitionException.class, () -> {
            rideService.updateRideStatus(1L, RideStatus.COMPLETED);
        });
    }

    @Test
    public void testValidTransitionToDriverArrivedAndStarted() {
        RideRequest ride = RideRequest.builder()
                .id(1L)
                .status(RideStatus.MATCHED)
                .build();

        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(i -> i.getArgument(0));

        RideRequest updated = rideService.updateRideStatus(1L, RideStatus.DRIVER_ARRIVED);
        assertEquals(RideStatus.DRIVER_ARRIVED, updated.getStatus());

        RideRequest started = rideService.updateRideStatus(1L, RideStatus.STARTED);
        assertEquals(RideStatus.STARTED, started.getStatus());
    }

    @Test
    public void testCancelStartedRideThrowsException() {
        RideRequest ride = RideRequest.builder()
                .id(1L)
                .status(RideStatus.STARTED)
                .build();

        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStateTransitionException.class, () -> {
            rideService.cancelRide(1L);
        });
    }

    @Test
    public void testCancelMatchedRideRefundsFare() {
        User passenger = User.builder()
                .id(10L)
                .name("Nusrat")
                .walletBalance(new BigDecimal("100.00"))
                .build();

        RidePool pool = RidePool.builder()
                .id(20L)
                .totalCapacity(3)
                .occupiedSeats(2)
                .status(PoolStatus.OPEN)
                .build();

        RideRequest ride = RideRequest.builder()
                .id(1L)
                .passenger(passenger)
                .pool(pool)
                .totalFare(new BigDecimal("60.00"))
                .requestedSeats(1)
                .status(RideStatus.MATCHED)
                .requestedAt(LocalDateTime.now())
                .build();

        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(i -> i.getArgument(0));

        RideRequest cancelled = rideService.cancelRide(1L);

        assertEquals(RideStatus.CANCELLED, cancelled.getStatus());
        assertEquals(new BigDecimal("160.00"), passenger.getWalletBalance());
        assertEquals(1, pool.getOccupiedSeats());
    }
}
