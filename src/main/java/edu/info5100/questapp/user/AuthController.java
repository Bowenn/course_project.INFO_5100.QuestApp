package edu.info5100.questapp.user;

import edu.info5100.questapp.config.JwtUtil;
import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.user.dto.LoginRequest;
import edu.info5100.questapp.user.dto.RegisterRequest;
import edu.info5100.questapp.user.dto.UserResponse;
import edu.info5100.questapp.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 * Supports both HTTP Basic Auth and JWT Bearer tokens.
 * Register is public; login generates JWT token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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
     * POST /api/auth/login
     * Login with email and password, returns JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(securityUser.getUsername(), securityUser.getUser().getRole().name());

        return ResponseEntity.ok(new LoginResponse(token, UserResponse.from(securityUser.getUser())));
    }

    /**
     * GET /api/auth/me
     * Return current authenticated user. Validates credentials via Basic Auth or JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(UserResponse.from(securityUser.getUser()));
    }
}
