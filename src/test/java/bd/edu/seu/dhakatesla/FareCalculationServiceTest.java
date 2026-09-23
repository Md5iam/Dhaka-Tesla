package bd.edu.seu.dhakatesla;

import bd.edu.seu.dhakatesla.dto.FareEstimateDTO;
import bd.edu.seu.dhakatesla.model.Zone;
import bd.edu.seu.dhakatesla.repository.ZoneRepository;
import bd.edu.seu.dhakatesla.service.FareCalculationService;
import bd.edu.seu.dhakatesla.service.impl.FareCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FareCalculationServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    private FareCalculationService fareCalculationService;

    @BeforeEach
    public void setup() {
        fareCalculationService = new FareCalculationServiceImpl(zoneRepository);
    }

    @Test
    public void testBaseFareAndPerKmCharge() {
        BigDecimal base = fareCalculationService.calculateBaseFare();
        assertEquals(new BigDecimal("30.00"), base);

        BigDecimal distanceCharge = fareCalculationService.calculateDistanceCharge(2.0, 1);
        assertEquals(new BigDecimal("30.00"), distanceCharge);
    }

    @Test
    public void testNusratFareCalculation() {
        Zone banani = Zone.builder().name("Banani").corridorGroup("Transit-Hub").distanceFromHubKm(0.0).build();
        Zone mohakhali = Zone.builder().name("Mohakhali").corridorGroup("Corridor-North-East").distanceFromHubKm(3.0).build();

        when(zoneRepository.findByName("Banani")).thenReturn(Optional.of(banani));
        when(zoneRepository.findByName("Mohakhali")).thenReturn(Optional.of(mohakhali));

        FareEstimateDTO estimate = fareCalculationService.estimateFare("Banani", "Mohakhali", 1);

        assertNotNull(estimate);
        assertEquals(3.0, estimate.getDistanceKm());
        assertEquals(new BigDecimal("30.00"), estimate.getBaseFare());
        assertEquals(new BigDecimal("45.00"), estimate.getDistanceCharge());
        assertEquals(new BigDecimal("75.00"), estimate.getSoloFare());
        assertEquals(new BigDecimal("15.00"), estimate.getPoolDiscount());
        assertEquals(new BigDecimal("60.00"), estimate.getPooledFare());
    }

    @Test
    public void testRafiqFareCalculation() {
        Zone banani = Zone.builder().name("Banani").corridorGroup("Transit-Hub").distanceFromHubKm(0.0).build();
        Zone gulshan1 = Zone.builder().name("Gulshan 1").corridorGroup("Corridor-North-East").distanceFromHubKm(2.5).build();

        when(zoneRepository.findByName("Banani")).thenReturn(Optional.of(banani));
        when(zoneRepository.findByName("Gulshan 1")).thenReturn(Optional.of(gulshan1));

        FareEstimateDTO estimate = fareCalculationService.estimateFare("Banani", "Gulshan 1", 1);

        assertNotNull(estimate);
        assertEquals(2.5, estimate.getDistanceKm());
        assertEquals(new BigDecimal("30.00"), estimate.getBaseFare());
        assertEquals(new BigDecimal("37.50"), estimate.getDistanceCharge());
        assertEquals(new BigDecimal("67.50"), estimate.getSoloFare());
        assertEquals(new BigDecimal("13.50"), estimate.getPoolDiscount());
        assertEquals(new BigDecimal("54.00"), estimate.getPooledFare());
    }
}
