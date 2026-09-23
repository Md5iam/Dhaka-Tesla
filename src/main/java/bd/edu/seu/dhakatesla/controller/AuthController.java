package bd.edu.seu.dhakatesla.controller;

import bd.edu.seu.dhakatesla.dto.ApiResponse;
import bd.edu.seu.dhakatesla.dto.LoginRequestDTO;
import bd.edu.seu.dhakatesla.dto.RegisterRequestDTO;
import bd.edu.seu.dhakatesla.dto.UserResponseDTO;
import bd.edu.seu.dhakatesla.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        UserResponseDTO user = userService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", user));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        UserResponseDTO user = userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", user));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserResponseById(id)));
    }

    @PostMapping("/users/{id}/topup")
    public ResponseEntity<ApiResponse<BigDecimal>> topUpWallet(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        BigDecimal newBalance = userService.topUpWallet(id, amount);
        return ResponseEntity.ok(ApiResponse.ok("Wallet recharged successfully", newBalance));
    }
}
