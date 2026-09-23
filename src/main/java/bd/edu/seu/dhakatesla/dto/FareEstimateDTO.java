package bd.edu.seu.dhakatesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareEstimateDTO {

    private String pickupZone;
    private String destinationZone;
    private Double distanceKm;
    private Integer requestedSeats;
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal soloFare;
    private BigDecimal poolDiscount;
    private BigDecimal pooledFare;
}
