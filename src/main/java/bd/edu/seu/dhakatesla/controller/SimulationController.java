package bd.edu.seu.dhakatesla.controller;

import bd.edu.seu.dhakatesla.dto.ApiResponse;
import bd.edu.seu.dhakatesla.dto.RideBookingRequestDTO;
import bd.edu.seu.dhakatesla.exception.CapacityExceededException;
import bd.edu.seu.dhakatesla.model.*;
import bd.edu.seu.dhakatesla.repository.RidePoolRepository;
import bd.edu.seu.dhakatesla.repository.RideRequestRepository;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.repository.VehicleRepository;
import bd.edu.seu.dhakatesla.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final RideRequestRepository rideRequestRepository;
    private final RidePoolRepository ridePoolRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final RideService rideService;

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> resetSimulation() {
        rideRequestRepository.deleteAll();
        ridePoolRepository.deleteAll();

        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle v : vehicles) {
            v.setStatus(VehicleStatus.ONLINE);
            vehicleRepository.save(v);
        }

        List<User> users = userRepository.findAll();
        for (User u : users) {
            u.setWalletBalance(new BigDecimal("500.00"));
            userRepository.save(u);
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "System reset to initial state");
        return ResponseEntity.ok(ApiResponse.ok("Reset complete", response));
    }

    @PostMapping("/rush-hour")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runRushHourSimulation() {
        resetSimulation();

        User nusrat = userRepository.findByEmail("nusrat@dhakatesla.com").orElse(null);
        User rafiq = userRepository.findByEmail("rafiq@dhakatesla.com").orElse(null);
        User shirin = userRepository.findByEmail("shirin@dhakatesla.com").orElse(null);

        List<Map<String, Object>> timeline = new ArrayList<>();

        if (nusrat != null) {
            RideBookingRequestDTO req1 = RideBookingRequestDTO.builder()
                    .passengerId(nusrat.getId())
                    .pickupZone("Banani")
                    .destinationZone("Mohakhali")
                    .requestedSeats(1)
                    .build();
            RideRequest r1 = rideService.bookRide(req1);
            Map<String, Object> step1 = new HashMap<>();
            step1.put("step", "8:41 AM - Nusrat books Banani to Mohakhali (1 seat)");
            step1.put("passenger", "Nusrat");
            step1.put("status", r1.getStatus());
            step1.put("fare", r1.getTotalFare());
            step1.put("poolId", r1.getPool().getId());
            step1.put("occupiedSeats", r1.getPool().getOccupiedSeats());
            timeline.add(step1);
        }

        if (rafiq != null) {
            RideBookingRequestDTO req2 = RideBookingRequestDTO.builder()
                    .passengerId(rafiq.getId())
                    .pickupZone("Banani")
                    .destinationZone("Gulshan 1")
                    .requestedSeats(1)
                    .build();
            RideRequest r2 = rideService.bookRide(req2);
            Map<String, Object> step2 = new HashMap<>();
            step2.put("step", "8:43 AM - Rafiq books Banani to Gulshan 1 (1 seat, pooled!)");
            step2.put("passenger", "Rafiq");
            step2.put("status", r2.getStatus());
            step2.put("fare", r2.getTotalFare());
            step2.put("poolId", r2.getPool().getId());
            step2.put("occupiedSeats", r2.getPool().getOccupiedSeats());
            timeline.add(step2);
        }

        if (shirin != null) {
            Map<String, Object> step3 = new HashMap<>();
            try {
                RideBookingRequestDTO req3Over = RideBookingRequestDTO.builder()
                        .passengerId(shirin.getId())
                        .pickupZone("Banani")
                        .destinationZone("Mohakhali")
                        .requestedSeats(2)
                        .build();
                rideService.bookRide(req3Over);
                step3.put("step", "8:44 AM - Shirin tried to book 2 seats (Unexpected success)");
            } catch (CapacityExceededException e) {
                step3.put("step", "8:44 AM - Shirin tried to book 2 seats (Capacity blocked safely: 2 + 2 > 3)");
                step3.put("passenger", "Shirin");
                step3.put("result", "REJECTED_CAPACITY_EXCEEDED");
                step3.put("reason", e.getMessage());
            }
            timeline.add(step3);

            RideBookingRequestDTO req3Single = RideBookingRequestDTO.builder()
                    .passengerId(shirin.getId())
                    .pickupZone("Banani")
                    .destinationZone("Mohakhali")
                    .requestedSeats(1)
                    .build();
            RideRequest r3 = rideService.bookRide(req3Single);
            Map<String, Object> step4 = new HashMap<>();
            step4.put("step", "8:44:30 AM - Shirin grabs the last 3rd seat (1 seat)");
            step4.put("passenger", "Shirin");
            step4.put("status", r3.getStatus());
            step4.put("fare", r3.getTotalFare());
            step4.put("poolId", r3.getPool().getId());
            step4.put("occupiedSeats", r3.getPool().getOccupiedSeats());
            step4.put("poolStatus", r3.getPool().getStatus());
            timeline.add(step4);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("simulation", "Banani Rush-Hour Story");
        result.put("timeline", timeline);
        return ResponseEntity.ok(ApiResponse.ok("Simulation executed successfully", result));
    }
}
