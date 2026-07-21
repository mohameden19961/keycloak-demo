# Keycloak 25

Serveur d'authentification open-source. Gère les utilisateurs, rôles et sessions OAuth2 / OpenID Connect.

## Configuration Docker

**Image :** `quay.io/keycloak/keycloak:25.0`

```yaml
environment:
  KC_DB: postgres
  KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak_db
  KC_DB_USERNAME: ${KC_DB_USERNAME}
  KC_DB_PASSWORD: ${KC_DB_PASSWORD}
  KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN}
  KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
  KC_HOSTNAME: https://localhost:18443
  KC_HOSTNAME_STRICT: "false"
  KC_PROXY_HEADERS: xforwarded
```

## Realm "Taks"

Le projet utilise un realm nommé **Taks** avec :

- **Client** : `task-api` (type `openid-connect`, accès via `confidential`)
- **Rôles** : `USER` (par défaut), `ADMIN`
- **Utilisateurs** : à créer dans l'admin console

### Obtenir un token

```bash
curl -X POST http://localhost:18082/realms/Taks/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=task-api" \
  -d "client_secret=<client-secret>" \
  -d "grant_type=password" \
  -d "username=<user>" \
  -d "password=<pass>"
```

### Utiliser le token

```bash
curl http://localhost:18090/api/tasks \
  -H "Authorization: Bearer <access_token>"
```

## Console admin

Accès : http://localhost:18082/admin — utilisateur défini dans `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`.
