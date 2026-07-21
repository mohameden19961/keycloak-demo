# Nginx

Reverse proxy HTTPS qui achemine les requêtes vers Keycloak et l'application.

## Configuration

**Image :** `nginx:alpine`  
**Fichier :** `nginx.conf`

### Règles de routage

| Path | Cible |
|---|---|
| `/realms/` | Keycloak (8080) |
| `/admin/` | Keycloak (8080) |
| `/resources/` | Keycloak (8080) |
| `/` | Application Spring Boot (8080) |

### HTTPS

- Redirection automatique HTTP → HTTPS (port 18443)
- Certificats auto-signés dans `certs/`
- Protocoles TLS 1.2 et 1.3
- Headers `X-Forwarded-*` transmis aux services

## Ports

| Port | Usage |
|---|---|
| `18088` | HTTP (redirection vers HTTPS) |
| `18443` | HTTPS |
