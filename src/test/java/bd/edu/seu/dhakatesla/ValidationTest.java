package bd.edu.seu.dhakatesla;

import bd.edu.seu.dhakatesla.dto.RegisterRequestDTO;
import bd.edu.seu.dhakatesla.dto.RideBookingRequestDTO;
import bd.edu.seu.dhakatesla.exception.ValidationException;
import bd.edu.seu.dhakatesla.model.UserRole;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.RideRequestRepository;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.service.FareCalculationService;
import bd.edu.seu.dhakatesla.service.PoolMatchingService;
import bd.edu.seu.dhakatesla.service.impl.RideServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ValidationTest {

    private Validator validator;

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
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        rideService = new RideServiceImpl(
                rideRequestRepository,
                ridePoolRepository,
                userRepository,
                fareCalculationService,
                poolMatchingService
        );
    }

    @Test
    public void testInvalidRegistrationEmail() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Test User")
                .email("not-an-email")
                .password("pass123")
                .role(UserRole.PASSENGER)
                .build();

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    public void testInvalidSeatBounds() {
        RideBookingRequestDTO tooFew = RideBookingRequestDTO.builder()
                .passengerId(1L)
                .pickupZone("Banani")
                .destinationZone("Mohakhali")
                .requestedSeats(0)
                .build();

        Set<ConstraintViolation<RideBookingRequestDTO>> violationsZero = validator.validate(tooFew);
        assertFalse(violationsZero.isEmpty());

        RideBookingRequestDTO tooMany = RideBookingRequestDTO.builder()
                .passengerId(1L)
                .pickupZone("Banani")
                .destinationZone("Mohakhali")
                .requestedSeats(4)
                .build();

        Set<ConstraintViolation<RideBookingRequestDTO>> violationsFour = validator.validate(tooMany);
        assertFalse(violationsFour.isEmpty());
    }

    @Test
    public void testSamePickupAndDestinationThrowsValidationException() {
        RideBookingRequestDTO sameZone = RideBookingRequestDTO.builder()
                .passengerId(1L)
                .pickupZone("Banani")
                .destinationZone("Banani")
                .requestedSeats(1)
                .build();

        assertThrows(ValidationException.class, () -> {
            rideService.bookRide(sameZone);
        });
    }
}
