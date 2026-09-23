package bd.edu.seu.dhakatesla.service;

import bd.edu.seu.dhakatesla.dto.LoginRequestDTO;
import bd.edu.seu.dhakatesla.dto.RegisterRequestDTO;
import bd.edu.seu.dhakatesla.dto.UserResponseDTO;
import bd.edu.seu.dhakatesla.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {
    UserResponseDTO login(LoginRequestDTO loginRequest);
    UserResponseDTO register(RegisterRequestDTO registerRequest);
    User getUserById(Long userId);
    UserResponseDTO getUserResponseById(Long userId);
    List<UserResponseDTO> getAllUsers();
    BigDecimal topUpWallet(Long userId, BigDecimal amount);
}
