# Keycloak 25

Serveur d'authentification open-source. Gère les utilisateurs, rôles et sessions OAuth2 / OpenID Connect.

## Mode production

Keycloak démarre en mode `start` (production) avec import automatique du realm :

```yaml
command: start --import-realm
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
volumes:
  - ./realm-config:/opt/keycloak/data/import:ro
```

## Realm "Taks" (auto-importé)

**Fichier :** `realm-config/taks-realm.json`

Le realm est importé automatiquement au démarrage via `--import-realm`.

- **Client** : `task-api` (public, `directAccessGrantsEnabled`, password flow)
- **Rôle par défaut** : `USER` (attribué automatiquement aux nouveaux utilisateurs)
- **Rôle ADMIN** : accès aux endpoints d'administration

### Utilisateurs pré-configurés

| Utilisateur | Mot de passe | Rôles |
|---|---|---|
| `user1` | `password123` | USER |
| `admin1` | `admin123` | ADMIN, USER |

### Obtenir un token

```bash
curl -X POST http://localhost:18082/realms/Taks/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=task-api" \
  -d "grant_type=password" \
  -d "username=user1" \
  -d "password=password123"
```

Le client `task-api` est **public** (pas de secret requis).

### Utiliser le token

```bash
curl http://localhost:18090/api/tasks \
  -H "Authorization: Bearer <access_token>"
```

## Console admin

Accès : http://localhost:18082/admin — identifiants définis dans `.env` (`KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`).
