package bd.edu.seu.dhakatesla.dto;

import bd.edu.seu.dhakatesla.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private BigDecimal walletBalance;
    private Long vehicleId;
    private String vehicleModelName;
    private Integer vehicleCapacity;
}
