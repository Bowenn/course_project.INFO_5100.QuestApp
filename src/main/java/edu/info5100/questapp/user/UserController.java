package edu.info5100.questapp.user;

import edu.info5100.questapp.security.JwtUtils;
import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.user.dto.UpdateProfileRequest;
import edu.info5100.questapp.user.dto.UpdateProfileResponse;
import edu.info5100.questapp.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-related endpoints.
 * GET /api/users/me     - current user profile (all roles)
 * GET /api/users/list   - list all USER-role accounts (authenticated)
 * GET /api/users        - all users including admins (ADMIN only)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * GET /api/users/me
     * Current user profile.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(UserResponse.from(securityUser.getUser()));
    }

    /**
     * PUT /api/users/me
     * Update current user's username, email, and/or password.
     * All fields optional (null = no change). Password change requires currentPassword.
     * Always returns a fresh JWT so the frontend can handle email changes gracefully.
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UpdateProfileResponse> updateMe(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        var updatedUser = userService.updateProfile(request, securityUser.getUser());
        String token = jwtUtils.generateToken(new SecurityUser(updatedUser));
        return ResponseEntity.ok(new UpdateProfileResponse(UserResponse.from(updatedUser), token));
    }

    /**
     * GET /api/users/list
     * List all users with role USER. Useful for task assignment.
     * Any authenticated user can call this.
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    /**
     * GET /api/users
     * List all users including admins. ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
