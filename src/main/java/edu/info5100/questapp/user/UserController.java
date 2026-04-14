package edu.info5100.questapp.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.user.dto.DepositRequest;
import edu.info5100.questapp.user.dto.UserResponse;
import edu.info5100.questapp.user.dto.WithdrawRequest;

/**
 * User-related endpoints.
 * /api/users/me - current user (all roles)
 * /api/users/takers - list of takers for giver to pick when assigning (GIVER only)
 * /api/users - all users (ADMIN only)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/me
     * Current user profile. Same as /api/auth/me for convenience.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(UserResponse.from(securityUser.getUser()));
    }

    /**
     * GET /api/users/takers
     * List all users with role TAKER. Used by giver when assigning a task.
     * GIVER and ADMIN only.
     */
    @GetMapping("/takers")
    @PreAuthorize("hasAnyRole('GIVER', 'ADMIN')")
    public ResponseEntity<List<UserResponse>> listTakers(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(userService.getTakers());
    }

    /**
     * GET /api/users
     * List all users. ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listAll(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * POST /api/users/deposit
     * Add balance to current user's account.
     */
    @PostMapping("/deposit")
    public ResponseEntity<UserResponse> deposit(
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(userService.deposit(securityUser.getUser(), request.amount()));
    }

    /**
     * POST /api/users/withdraw
     * Withdraw balance from current user's account.
     */
    @PostMapping("/withdraw")
    public ResponseEntity<UserResponse> withdraw(
            @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(userService.withdraw(securityUser.getUser(), request.amount()));
    }
}
