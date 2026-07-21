# API Documentation

Base URL: `https://localhost:18443`

## Authentication

Keycloak (OAuth2 / OpenID Connect) avec le client public `task-api`.

### Obtenir un token

```bash
curl -s -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -k \
  -d "client_id=task-api" \
  -d "grant_type=password" \
  -d "username=<username>" \
  -d "password=<password>"
```

Réponse : `{ "access_token": "...", "refresh_token": "...", ... }`

### Utiliser le token

Ajouter l'en-tête `Authorization: Bearer <access_token>` à chaque requête.

## Endpoints

### Health

```
GET /api/public/health
```

- **Auth** : ❌ Aucune
- **Réponse** : `200 OK` `ok`

### Inscription

```
POST /api/auth/register
Content-Type: application/json
```

- **Auth** : ❌ Aucune
- **Body** :

```json
{
  "username": "nouvel_utilisateur",
  "email": "user@example.com",
  "password": "monMotDePasse",
  "firstName": "Prénom",
  "lastName": "Nom"
}
```

> `firstName` et `lastName` sont optionnels. Par défaut, `firstName = username`, `lastName = firstName`.

- **Processus interne** :
  1. Création de l'utilisateur dans Keycloak (POST `/admin/realms/Taks/users`)
  2. Assignation du rôle `USER` (POST `/admin/realms/Taks/users/{id}/role-mappings/realm`)
- **Réponse** : `201 Created` `{ "message": "Utilisateur créé avec succès" }`
- **Erreurs** : `409 Conflict` si l'utilisateur existe déjà

### Créer une tâche

```
POST /api/tasks
Content-Type: application/json
Authorization: Bearer <token>
```

- **Auth** : ✅ JWT (USER)
- **Body** :

```json
{
  "title": "Ma tâche",
  "done": false
}
```

- **Réponse** : `201 Created` `{ "id": 1, "title": "...", "done": false, "ownerId": "<username>" }`
- `ownerId` est automatiquement extrait du JWT (`preferred_username`).

### Lister les tâches

```
GET /api/tasks
Authorization: Bearer <token>
```

- **Auth** : ✅ JWT (USER)
- **Réponse** : `200 OK` `[ { "id": 1, "title": "...", ... } ]`

### Supprimer sa propre tâche

```
DELETE /api/tasks/{id}
Authorization: Bearer <token>
```

- **Auth** : ✅ JWT (USER - propriétaire uniquement)
- **Réponse** : `200 OK`
- **Erreurs** : `403 Forbidden` si l'utilisateur n'est pas le propriétaire

### Supprimer toute tâche (Admin)

```
DELETE /api/admin/tasks/{id}
Authorization: Bearer <token>
```

- **Auth** : ✅ JWT (rôle `ADMIN`)
- **Réponse** : `200 OK`
- **Erreurs** : `403 Forbidden` si l'utilisateur n'a pas le rôle ADMIN

## Modèle Task

```json
{
  "id": 1,
  "title": "Apprendre Keycloak",
  "done": false,
  "ownerId": "user1"
}
```

## Codes d'erreur

| Code | Description |
|------|-------------|
| 401 | Token manquant ou invalide |
| 403 | Accès interdit (mauvais rôle ou pas propriétaire) |
| 404 | Ressource introuvable |
| 409 | Conflit (utilisateur déjà existant) |

## Exemple complet (curl)

```bash
# 1. Token
TOKEN=$(curl -sk -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -d "client_id=task-api" -d "grant_type=password" \
  -d "username=user1" -d "password=password123" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

# 2. Créer tâche
TASK_ID=$(curl -sk -X POST https://localhost:18443/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['id'])")

# 3. Lister
curl -sk https://localhost:18443/api/tasks -H "Authorization: Bearer $TOKEN"

# 4. Supprimer
curl -sk -X DELETE "https://localhost:18443/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN"
```

> L'option `-k` (ou `--insecure`) est nécessaire avec des certificats SSL auto-signés.
