package edu.info5100.questapp.user;

import edu.info5100.questapp.exception.BadRequestException;
import edu.info5100.questapp.exception.ResourceNotFoundException;
import edu.info5100.questapp.user.dto.RegisterRequest;
import edu.info5100.questapp.user.dto.UpdateProfileRequest;
import edu.info5100.questapp.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * Register a new user. Role is always set to USER — users cannot self-assign ADMIN.
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
            Role.USER
        );
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    /**
     * Update username, email, and/or password for the current user.
     * Only non-null, non-blank fields are applied.
     * Password change requires currentPassword to be verified first.
     */
    @Transactional
    public User updateProfile(UpdateProfileRequest request, User currentUser) {
        var user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));

        // Password change: verify current password first
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        // Username change
        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new BadRequestException("Username already taken");
            }
            user.setUsername(request.username());
        }

        // Email change
        if (request.email() != null && !request.email().isBlank()
                && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BadRequestException("Email already registered");
            }
            user.setEmail(request.email());
        }

        return userRepository.save(user);
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
     * Get all users with role USER (for task assignment dropdown).
     */
    public List<UserResponse> getUsers() {
        return userRepository.findByRole(Role.USER).stream()
            .map(UserResponse::from)
            .toList();
    }

    /**
     * Get all users (admin only).
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .toList();
    }
}
