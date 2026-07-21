package com.testing.test.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    private final RestTemplate restTemplate;

    @Value("${keycloak.admin.server-url:http://keycloak:8080}")
    private String serverUrl;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    @Value("${keycloak.admin.realm:Taks}")
    private String realm;

    public KeycloakAdminService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createUser(String username, String email, String password, String firstName, String lastName) {
        String token = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
            {
                "username": "%s",
                "email": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "enabled": true,
                "emailVerified": true,
                "requiredActions": [],
                "credentials": [{"type": "password", "value": "%s", "temporary": false}]
            }
            """.formatted(username, email, firstName, lastName, password);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            serverUrl + "/admin/realms/" + realm + "/users",
            HttpMethod.POST,
            request,
            String.class
        );

        String location = response.getHeaders().getLocation().toString();
        String userId = location.substring(location.lastIndexOf("/") + 1);

        assignRealmRole(token, userId, "USER");
    }

    private void assignRealmRole(String token, String userId, String roleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List> rolesResponse = restTemplate.exchange(
            serverUrl + "/admin/realms/" + realm + "/roles",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            List.class
        );

        List<Map<String, Object>> roles = rolesResponse.getBody();
        String roleId = null;
        for (Map<String, Object> role : roles) {
            if (roleName.equals(role.get("name"))) {
                roleId = (String) role.get("id");
                break;
            }
        }

        String roleBody = """
            [{"id": "%s", "name": "%s"}]
            """.formatted(roleId, roleName);

        HttpEntity<String> request = new HttpEntity<>(roleBody, headers);
        restTemplate.exchange(
            serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm",
            HttpMethod.POST,
            request,
            String.class
        );
    }

    private String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        body.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            serverUrl + "/realms/master/protocol/openid-connect/token",
            HttpMethod.POST,
            request,
            Map.class
        );
        return (String) response.getBody().get("access_token");
    }
}
