package bd.edu.seu.dhakatesla.dto;

import bd.edu.seu.dhakatesla.model.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideStatusUpdateDTO {

    @NotNull(message = "Ride status is required")
    private RideStatus status;
}
