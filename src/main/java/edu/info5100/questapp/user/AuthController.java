package edu.info5100.questapp.user;

import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.user.dto.LoginRequest;
import edu.info5100.questapp.user.dto.RegisterRequest;
import edu.info5100.questapp.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 * Uses HTTP Basic Auth: client sends Authorization: Basic base64(email:password).
 * Register is public; all other API endpoints require authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/register
     * Register a new user. Public endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    /**
     * GET /api/auth/me
     * Return current authenticated user. Validates credentials via Basic Auth.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(UserResponse.from(securityUser.getUser()));
    }
}
