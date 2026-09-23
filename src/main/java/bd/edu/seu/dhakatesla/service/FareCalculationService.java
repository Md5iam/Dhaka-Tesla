package bd.edu.seu.dhakatesla.service;

import bd.edu.seu.dhakatesla.dto.FareEstimateDTO;

import java.math.BigDecimal;

public interface FareCalculationService {
    Double calculateDistance(String pickupZone, String destinationZone);
    FareEstimateDTO estimateFare(String pickupZone, String destinationZone, int requestedSeats);
    BigDecimal calculateBaseFare();
    BigDecimal calculateDistanceCharge(Double distanceKm, int seats);
    BigDecimal calculatePoolDiscount(BigDecimal subtotal);
}
