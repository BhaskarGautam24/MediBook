package com.medibook.auth.controller;

import com.medibook.auth.entity.User;
import com.medibook.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin user management endpoints.
 * Distributed to auth-service since it owns the User entity.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{id}/suspend")
    @Operation(summary = "Suspend a user", description = "Suspends a user account by ID")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable Long id) {
        userService.suspendUser(id);
        return ResponseEntity.ok(Map.of("message", "User suspended"));
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate a user", description = "Reactivates a suspended user account")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "User activated"));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user", description = "Permanently deletes a user account")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
