package bd.edu.seu.dhakatesla.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideBookingRequestDTO {

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    @NotBlank(message = "Pickup zone is required")
    private String pickupZone;

    @NotBlank(message = "Destination zone is required")
    private String destinationZone;

    @NotNull(message = "Requested seats is required")
    @Min(value = 1, message = "At least 1 seat must be requested")
    @Max(value = 3, message = "Cannot request more than 3 seats")
    private Integer requestedSeats;
}
