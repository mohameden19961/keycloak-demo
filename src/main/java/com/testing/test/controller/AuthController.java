package com.testing.test.controller;

import com.testing.test.service.KeycloakAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakAdminService adminService;
    private final RestTemplate restTemplate;

    @Value("${keycloak.admin.server-url:http://keycloak:8080}")
    private String serverUrl;

    @Value("${keycloak.admin.realm:Taks}")
    private String realm;

    public AuthController(KeycloakAdminService adminService, RestTemplate restTemplate) {
        this.adminService = adminService;
        this.restTemplate = restTemplate;
    }

    private String getTokenUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.getOrDefault("firstName", username);
        String lastName = body.getOrDefault("lastName", firstName);

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username est requis"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email est requis"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "password doit contenir au moins 6 caractères"));
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh_token est requis"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", "task-api");
            form.add("grant_type", "refresh_token");
            form.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                getTokenUrl(), HttpMethod.POST, request, Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Refresh token invalide ou expiré"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh_token est requis"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", "task-api");
            form.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
            restTemplate.exchange(
                serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout",
                HttpMethod.POST, request, String.class
            );
            return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Erreur lors de la déconnexion"
            ));
        }
    }
}
