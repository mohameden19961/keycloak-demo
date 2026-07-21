package com.testing.test.controller;

import com.testing.test.service.KeycloakAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakAdminService adminService;

    public AuthController(KeycloakAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.getOrDefault("firstName", username);
        String lastName = body.getOrDefault("lastName", firstName);

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "username, email et password sont requis"
            ));
        }

        try {
            adminService.createUser(username, email, password, firstName, lastName);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Utilisateur créé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Erreur lors de la création : " + e.getMessage()
            ));
        }
    }
}
