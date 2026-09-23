package bd.edu.seu.dhakatesla.service.impl;

import bd.edu.seu.dhakatesla.dto.LoginRequestDTO;
import bd.edu.seu.dhakatesla.dto.RegisterRequestDTO;
import bd.edu.seu.dhakatesla.dto.UserResponseDTO;
import bd.edu.seu.dhakatesla.exception.ResourceNotFoundException;
import bd.edu.seu.dhakatesla.exception.ValidationException;
import bd.edu.seu.dhakatesla.model.User;
import bd.edu.seu.dhakatesla.model.UserRole;
import bd.edu.seu.dhakatesla.model.Vehicle;
import bd.edu.seu.dhakatesla.model.VehicleStatus;
import bd.edu.seu.dhakatesla.repository.UserRepository;
import bd.edu.seu.dhakatesla.repository.VehicleRepository;
import bd.edu.seu.dhakatesla.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new ValidationException("Invalid email or password");
        }

        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        String cleanEmail = registerRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ValidationException("An account with this email already exists");
        }

        User user = User.builder()
                .name(registerRequest.getName().trim())
                .email(cleanEmail)
                .password(registerRequest.getPassword())
                .phone(registerRequest.getPhone())
                .role(registerRequest.getRole())
                .walletBalance(new BigDecimal("500.00"))
                .build();

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == UserRole.DRIVER) {
            String model = registerRequest.getVehicleModelName() != null && !registerRequest.getVehicleModelName().isBlank()
                    ? registerRequest.getVehicleModelName().trim()
                    : "Bullet";
            String plate = registerRequest.getVehicleLicensePlate() != null && !registerRequest.getVehicleLicensePlate().isBlank()
                    ? registerRequest.getVehicleLicensePlate().trim()
                    : "DHAKA-TESLA-" + (1000 + savedUser.getId());

            Vehicle vehicle = Vehicle.builder()
                    .modelName(model)
                    .licensePlate(plate)
                    .capacity(3)
                    .status(VehicleStatus.ONLINE)
                    .driver(savedUser)
                    .build();

            vehicleRepository.save(vehicle);
        }

        return mapToDTO(savedUser);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public UserResponseDTO getUserResponseById(Long userId) {
        User user = getUserById(userId);
        return mapToDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigDecimal topUpWallet(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Top up amount must be positive");
        }
        User user = getUserById(userId);
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);
        return user.getWalletBalance();
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO.UserResponseDTOBuilder builder = UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .walletBalance(user.getWalletBalance());

        if (user.getRole() == UserRole.DRIVER) {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(user.getId());
            if (vehicleOpt.isPresent()) {
                Vehicle v = vehicleOpt.get();
                builder.vehicleId(v.getId())
                        .vehicleModelName(v.getModelName())
                        .vehicleCapacity(v.getCapacity());
            }
        }

        return builder.build();
    }
}
