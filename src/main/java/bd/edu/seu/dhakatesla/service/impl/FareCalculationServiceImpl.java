package bd.edu.seu.dhakatesla.service.impl;

import bd.edu.seu.dhakatesla.dto.FareEstimateDTO;
import bd.edu.seu.dhakatesla.model.Zone;
import bd.edu.seu.dhakatesla.repository.ZoneRepository;
import bd.edu.seu.dhakatesla.service.FareCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FareCalculationServiceImpl implements FareCalculationService {

    private final ZoneRepository zoneRepository;

    private static final BigDecimal BASE_FARE = new BigDecimal("30.00");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("15.00");
    private static final BigDecimal POOL_DISCOUNT_PERCENT = new BigDecimal("0.20");

    @Override
    public Double calculateDistance(String pickupZone, String destinationZone) {
        if (pickupZone.equalsIgnoreCase(destinationZone)) {
            return 1.0;
        }

        Optional<Zone> pickup = zoneRepository.findByName(pickupZone);
        Optional<Zone> dropoff = zoneRepository.findByName(destinationZone);

        if (pickup.isPresent() && dropoff.isPresent()) {
            double d1 = pickup.get().getDistanceFromHubKm();
            double d2 = dropoff.get().getDistanceFromHubKm();
            double dist = Math.abs(d1 - d2);
            if (dist < 1.0) {
                dist = 2.0;
            }
            return BigDecimal.valueOf(dist).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        return 3.0;
    }

    @Override
    public FareEstimateDTO estimateFare(String pickupZone, String destinationZone, int requestedSeats) {
        Double distanceKm = calculateDistance(pickupZone, destinationZone);
        BigDecimal base = calculateBaseFare();
        BigDecimal distanceCharge = calculateDistanceCharge(distanceKm, requestedSeats);
        BigDecimal soloFare = base.add(distanceCharge).setScale(2, RoundingMode.HALF_UP);
        BigDecimal poolDiscount = calculatePoolDiscount(soloFare);
        BigDecimal pooledFare = soloFare.subtract(poolDiscount).setScale(2, RoundingMode.HALF_UP);

        return FareEstimateDTO.builder()
                .pickupZone(pickupZone)
                .destinationZone(destinationZone)
                .distanceKm(distanceKm)
                .requestedSeats(requestedSeats)
                .baseFare(base)
                .distanceCharge(distanceCharge)
                .soloFare(soloFare)
                .poolDiscount(poolDiscount)
                .pooledFare(pooledFare)
                .build();
    }

    @Override
    public BigDecimal calculateBaseFare() {
        return BASE_FARE;
    }

    @Override
    public BigDecimal calculateDistanceCharge(Double distanceKm, int seats) {
        return PER_KM_RATE.multiply(BigDecimal.valueOf(distanceKm))
                .multiply(BigDecimal.valueOf(seats))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculatePoolDiscount(BigDecimal subtotal) {
        return subtotal.multiply(POOL_DISCOUNT_PERCENT).setScale(2, RoundingMode.HALF_UP);
    }
}
