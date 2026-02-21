package edu.info5100.questapp.user;

import edu.info5100.questapp.exception.BadRequestException;
import edu.info5100.questapp.exception.ResourceNotFoundException;
import edu.info5100.questapp.user.dto.RegisterRequest;
import edu.info5100.questapp.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user registration and lookup.
 * Authentication is handled by Spring Security (UserDetailsService).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user. Validates uniqueness of email and username.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already taken");
        }

        var user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.role()
        );
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    /**
     * Get user by ID. Throws if not found.
     */
    public User getById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Get user by email (for authentication).
     */
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email));
    }

    /**
     * Get all users with role TAKER (for giver to pick when assigning).
     */
    public java.util.List<UserResponse> getTakers() {
        return userRepository.findByRole(Role.TAKER).stream()
            .map(UserResponse::from)
            .toList();
    }

    /**
     * Get all users (admin only).
     */
    public java.util.List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .toList();
    }
}
