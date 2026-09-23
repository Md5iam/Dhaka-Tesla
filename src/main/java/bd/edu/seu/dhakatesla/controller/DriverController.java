package bd.edu.seu.dhakatesla.controller;

import bd.edu.seu.dhakatesla.dto.ApiResponse;
import bd.edu.seu.dhakatesla.model.PoolStatus;
import bd.edu.seu.dhakatesla.model.RidePool;
import bd.edu.seu.dhakatesla.model.RideRequest;
import bd.edu.seu.dhakatesla.model.Vehicle;
import bd.edu.seu.dhakatesla.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/{driverId}/toggle-online")
    public ResponseEntity<ApiResponse<Vehicle>> toggleOnline(
            @PathVariable Long driverId,
            @RequestBody Map<String, Boolean> request) {
        boolean online = request.getOrDefault("online", true);
        Vehicle vehicle = driverService.toggleDriverOnline(driverId, online);
        return ResponseEntity.ok(ApiResponse.ok("Driver status updated", vehicle));
    }

    @GetMapping("/{driverId}/vehicle")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(@PathVariable Long driverId) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getDriverVehicle(driverId)));
    }

    @GetMapping("/{driverId}/active-pool")
    public ResponseEntity<ApiResponse<RidePool>> getActivePool(@PathVariable Long driverId) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getActivePoolForDriver(driverId)));
    }

    @GetMapping("/{driverId}/passengers")
    public ResponseEntity<ApiResponse<List<RideRequest>>> getActivePassengers(@PathVariable Long driverId) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getPassengersInActivePool(driverId)));
    }

    @PostMapping("/pools/{poolId}/advance")
    public ResponseEntity<ApiResponse<RidePool>> advancePoolStage(
            @PathVariable Long poolId,
            @RequestBody Map<String, String> request) {
        PoolStatus targetStatus = PoolStatus.valueOf(request.get("targetStatus"));
        RidePool pool = driverService.advancePoolStage(poolId, targetStatus);
        return ResponseEntity.ok(ApiResponse.ok("Pool stage advanced", pool));
    }
}
