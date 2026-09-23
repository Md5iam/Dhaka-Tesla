package bd.edu.seu.dhakatesla.controller;

import bd.edu.seu.dhakatesla.dto.ApiResponse;
import bd.edu.seu.dhakatesla.dto.FareEstimateDTO;
import bd.edu.seu.dhakatesla.dto.RideBookingRequestDTO;
import bd.edu.seu.dhakatesla.dto.RideStatusUpdateDTO;
import bd.edu.seu.dhakatesla.model.RideRequest;
import bd.edu.seu.dhakatesla.service.FareCalculationService;
import bd.edu.seu.dhakatesla.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RideController {

    private final RideService rideService;
    private final FareCalculationService fareCalculationService;

    @GetMapping("/estimate")
    public ResponseEntity<ApiResponse<FareEstimateDTO>> estimateFare(
            @RequestParam String pickupZone,
            @RequestParam String destinationZone,
            @RequestParam(defaultValue = "1") int requestedSeats) {
        FareEstimateDTO estimate = fareCalculationService.estimateFare(pickupZone, destinationZone, requestedSeats);
        return ResponseEntity.ok(ApiResponse.ok(estimate));
    }

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<RideRequest>> bookRide(@Valid @RequestBody RideBookingRequestDTO bookingRequest) {
        RideRequest ride = rideService.bookRide(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Ride booked and matched successfully", ride));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RideRequest>> getRideById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.getRideById(id)));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<ApiResponse<List<RideRequest>>> getPassengerRides(@PathVariable Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.getPassengerRides(passengerId)));
    }

    @GetMapping("/passenger/{passengerId}/active")
    public ResponseEntity<ApiResponse<RideRequest>> getActivePassengerRide(@PathVariable Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.getActivePassengerRide(passengerId)));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RideRequest>> updateRideStatus(
            @PathVariable Long id,
            @Valid @RequestBody RideStatusUpdateDTO statusUpdate) {
        RideRequest ride = rideService.updateRideStatus(id, statusUpdate.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Ride status updated", ride));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RideRequest>> cancelRide(@PathVariable Long id) {
        RideRequest ride = rideService.cancelRide(id);
        return ResponseEntity.ok(ApiResponse.ok("Ride cancelled and fare refunded", ride));
    }
}
