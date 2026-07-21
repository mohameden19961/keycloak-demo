# API Documentation

Base URL: `https://localhost:18443`

## Authentication

Keycloak (OAuth2 / OpenID Connect) avec le client public `task-api`.

### Obtenir un token

```bash
curl -sk -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -d "client_id=task-api" \
  -d "grant_type=password" \
  -d "username=<username>" \
  -d "password=<password>"
```

Réponse : `{ "access_token": "...", "refresh_token": "...", ... }`

### Utiliser le token

Ajouter l'en-tête `Authorization: Bearer <access_token>` à chaque requête.

---

## Endpoints

### Health

```
GET /api/public/health
```

- **Auth** : ❌ Aucune
- **Réponse** : `200` `ok`

---

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

- `firstName` et `lastName` sont optionnels (défaut : `firstName = username`, `lastName = firstName`)
- `password` : minimum 6 caractères
- **Processus interne** :
  1. Création de l'utilisateur dans Keycloak (POST `/admin/realms/Taks/users`)
  2. Assignation du rôle `USER` (POST `/users/{id}/role-mappings/realm`)
- **Réponse** : `201` `{ "message": "Utilisateur créé avec succès" }`
- **Erreurs** : `400` si champs manquants ou invalides, `409` si utilisateur existe

---

### Rafraîchir le token

```
POST /api/auth/refresh
Content-Type: application/json
```

- **Auth** : ❌ Aucune
- **Body** : `{ "refresh_token": "..." }`
- **Réponse** : `200` `{ "access_token": "...", "refresh_token": "...", ... }`
- **Erreurs** : `401` si refresh_token invalide ou expiré

---

### Déconnexion

```
POST /api/auth/logout
Content-Type: application/json
```

- **Auth** : ❌ Aucune
- **Body** : `{ "refresh_token": "..." }`
- **Effet** : Invalide le refresh_token côté Keycloak
- **Réponse** : `200` `{ "message": "Déconnexion réussie" }`

---

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

- `title` : requis, ne peut pas être vide
- `ownerId` automatiquement extrait du JWT (`preferred_username`)
- **Réponse** : `201` `{ "id": 1, "title": "...", "done": false, "ownerId": "user1" }`
- **Erreurs** : `400` si `title` est vide

---

### Lister ses propres tâches

```
GET /api/tasks
Authorization: Bearer <token>
```

- **Auth** : ✅ JWT (USER)
- **Isolation** : retourne uniquement les tâches de l'utilisateur connecté
- **Réponse** : `200` `[ { "id": 1, "title": "...", ... } ]`

---

### Supprimer sa propre tâche

```
DELETE /api/tasks/{id}
Authorization: Bearer <token>
```

- **Auth** : ✅ JWT (USER - propriétaire uniquement)
- **Réponse** : `200`
- **Erreurs** : `403` si pas le propriétaire

---

### Admin : lister toutes les tâches

```
GET /api/admin/tasks
Authorization: Bearer <token>
```

- **Auth** : ✅ ADMIN
- **Réponse** : `200` `[ { "id": 1, "title": "...", "ownerId": "...", ... } ]`
- **Erreurs** : `403` si rôle USER

---

### Admin : supprimer toute tâche

```
DELETE /api/admin/tasks/{id}
Authorization: Bearer <token>
```

- **Auth** : ✅ ADMIN
- **Réponse** : `200`
- **Erreurs** : `403` si rôle USER

---

## Modèle Task

```json
{
  "id": 1,
  "title": "Apprendre Keycloak",
  "done": false,
  "ownerId": "user1"
}
```

---

## Codes d'erreur

| Code | Description |
|------|-------------|
| 400 | Validation échouée (champ requis, titre vide, password trop court) |
| 401 | Token manquant, invalide ou expiré |
| 403 | Accès interdit (mauvais rôle, pas propriétaire) |
| 404 | Ressource introuvable |
| 409 | Conflit (utilisateur déjà existant) |
| 429 | Trop de requêtes (rate limit : 100 req/min par IP) |

---

## Rate limiting

- **Limite** : 100 requêtes par minute par adresse IP
- **Réponse** : `429 Too Many Requests` `{ "error": "Trop de requêtes. Réessayez dans 60 secondes." }`

---

## Exemple complet (curl)

```bash
# 1. Token
TOKEN=$(curl -sk -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -d "client_id=task-api" -d "grant_type=password" \
  -d "username=user1" -d "password=password123" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

RT=$(curl -sk -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -d "client_id=task-api" -d "grant_type=password" \
  -d "username=user1" -d "password=password123" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['refresh_token'])")

# 2. Créer tâche
TASK_ID=$(curl -sk -X POST https://localhost:18443/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['id'])")

# 3. Lister ses tâches
curl -sk https://localhost:18443/api/tasks -H "Authorization: Bearer $TOKEN"

# 4. Rafraîchir le token
curl -sk -X POST https://localhost:18443/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh_token\":\"$RT\"}"

# 5. Déconnexion
curl -sk -X POST https://localhost:18443/api/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refresh_token\":\"$RT\"}"

# 6. Supprimer
curl -sk -X DELETE "https://localhost:18443/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN"
```

> L'option `-k` (ou `--insecure`) est nécessaire avec des certificats SSL auto-signés.
