package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.UserCreateRequest;
import com.hqc.hophuddles.dto.response.UserResponse;
import com.hqc.hophuddles.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth0/{auth0Id}")
    public ResponseEntity<UserResponse> getUserByAuth0Id(@PathVariable String auth0Id) {
        UserResponse response = userService.getUserByAuth0Id(auth0Id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<List<UserResponse>> getUsersByAgency(@PathVariable Long agencyId) {
        List<UserResponse> users = userService.getUsersByAgency(agencyId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/agency/{agencyId}/search")
    public ResponseEntity<Page<UserResponse>> searchUsersByAgency(
            @PathVariable Long agencyId,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<UserResponse> users = userService.searchUsersByAgency(agencyId, searchTerm, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/login")
    public ResponseEntity<Void> updateLastLogin(@PathVariable Long userId) {
        userService.updateLastLogin(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}