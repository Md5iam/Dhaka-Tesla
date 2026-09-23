package bd.edu.seu.dhakatesla.config;

import bd.edu.seu.dhakatesla.model.*;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.repository.VehicleRepository;
import bd.edu.seu.dhakatesla.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ZoneRepository zoneRepository;

    @Override
    public void run(String... args) {
        seedZones();
        seedUsersAndVehicles();
    }

    private void seedZones() {
        if (zoneRepository.count() == 0) {
            List<Zone> zones = List.of(
                    Zone.builder().name("Banani").corridorGroup("Transit-Hub").distanceFromHubKm(0.0).build(),
                    Zone.builder().name("Mohakhali").corridorGroup("Corridor-North-East").distanceFromHubKm(3.0).build(),
                    Zone.builder().name("Gulshan 1").corridorGroup("Corridor-North-East").distanceFromHubKm(2.5).build(),
                    Zone.builder().name("Gulshan 2").corridorGroup("Corridor-North-East").distanceFromHubKm(2.0).build(),
                    Zone.builder().name("Farmgate").corridorGroup("Corridor-Central-West").distanceFromHubKm(5.5).build(),
                    Zone.builder().name("Dhanmondi").corridorGroup("Corridor-Central-West").distanceFromHubKm(8.0).build(),
                    Zone.builder().name("Mirpur").corridorGroup("Corridor-North-West").distanceFromHubKm(7.5).build(),
                    Zone.builder().name("Uttara").corridorGroup("Corridor-North").distanceFromHubKm(10.0).build()
            );
            zoneRepository.saveAll(zones);
        }
    }

    private void seedUsersAndVehicles() {
        if (userRepository.count() == 0) {
            User jashim = User.builder()
                    .name("Jashim")
                    .email("jashim@dhakatesla.com")
                    .password("password123")
                    .phone("+8801711000001")
                    .role(UserRole.DRIVER)
                    .walletBalance(new BigDecimal("150.00"))
                    .build();
            User savedDriver = userRepository.save(jashim);

            Vehicle bullet = Vehicle.builder()
                    .modelName("Bullet")
                    .licensePlate("DHAKA-METRO-TA-1122")
                    .capacity(3)
                    .status(VehicleStatus.ONLINE)
                    .driver(savedDriver)
                    .build();
            vehicleRepository.save(bullet);

            User nusrat = User.builder()
                    .name("Nusrat")
                    .email("nusrat@dhakatesla.com")
                    .password("password123")
                    .phone("+8801811000002")
                    .role(UserRole.PASSENGER)
                    .walletBalance(new BigDecimal("500.00"))
                    .build();

            User rafiq = User.builder()
                    .name("Rafiq")
                    .email("rafiq@dhakatesla.com")
                    .password("password123")
                    .phone("+8801911000003")
                    .role(UserRole.PASSENGER)
                    .walletBalance(new BigDecimal("500.00"))
                    .build();

            User shirin = User.builder()
                    .name("Shirin")
                    .email("shirin@dhakatesla.com")
                    .password("password123")
                    .phone("+8801611000004")
                    .role(UserRole.PASSENGER)
                    .walletBalance(new BigDecimal("500.00"))
                    .build();

            userRepository.saveAll(List.of(nusrat, rafiq, shirin));
        }
    }
}
