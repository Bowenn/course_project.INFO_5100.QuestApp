package edu.info5100.questapp.user;

import edu.info5100.questapp.security.JwtUtils;
import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.user.dto.LoginRequest;
import edu.info5100.questapp.user.dto.LoginResponse;
import edu.info5100.questapp.user.dto.RegisterRequest;
import edu.info5100.questapp.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 * POST /api/auth/register - public, creates a USER account
 * POST /api/auth/login    - public, returns a JWT token
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * POST /api/auth/register
     * Register a new user. Public endpoint. Role is always USER.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    /**
     * POST /api/auth/login
     * Authenticate with email + password. Returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var userDetails = (SecurityUser) userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtils.generateToken(userDetails);
        var user = userDetails.getUser();

        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getRole().name()));
    }

}
