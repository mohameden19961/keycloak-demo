# Keycloak Demo

API REST sécurisée Spring Boot 3 + Keycloak 25 + PostgreSQL 16, proxyfiée par Nginx avec HTTPS, CI/CD GitHub Actions.

## Stack

| Technologie | Rôle |
|---|---|
| [Spring Boot 3](docs/spring-boot.md) | API RESTful avec OAuth2 / JWT |
| [Keycloak 25](docs/keycloak.md) | Serveur d'authentification et gestion des rôles |
| [PostgreSQL 16](docs/postgresql.md) | Base de données relationnelle |
| [Nginx](docs/nginx.md) | Reverse proxy HTTPS |
| [Docker](docs/docker.md) | Conteneurisation multi-stage + Docker Compose |
| GitHub Actions | CI/CD (Build, Tests, OWASP, Trivy, Newman) |
| [SonarQube](docs/sonarqube.md) | Analyse statique de code |
| [OWASP Dependency Check](docs/owasp.md) | Scan des vulnérabilités des dépendances |
| [Trivy](docs/trivy.md) | Scan des vulnérabilités des containers |
| [Newman](docs/newman.md) | Tests d'intégration API Postman |

---

## Guide complet d'installation

### Prérequis

- Docker (version 24+)
- Docker Compose (ou plugin `docker compose`)
- Git
- OpenSSL (pour générer les certificats)
- make (optionnel)

### 1. Cloner le projet

```bash
git clone https://github.com/mohameden19961/keycloak-demo.git
cd keycloak-demo
```

### 2. Configurer les secrets

```bash
cp .env.example .env
```

Éditer `.env` et remplacer les valeurs par défaut :

```env
POSTGRES_USER=abdy
POSTGRES_PASSWORD=changeme          # Mot de passe PostgreSQL (app)
KC_DB_USERNAME=abdy
KC_DB_PASSWORD=changeme             # Mot de passe PostgreSQL (Keycloak)
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=changeme    # Mot de passe admin Keycloak
SPRING_DATASOURCE_USERNAME=abdy
SPRING_DATASOURCE_PASSWORD=changeme # Mot de passe PostgreSQL (Spring Boot)
SONAR_TOKEN=your_sonar_token        # Token SonarQube (optionnel)
SONAR_HOST_URL=http://172.17.0.1:19000
NVD_API_KEY=your_nvd_api_key        # Clé API NVD (optionnelle)
```

### 3. Générer les certificats SSL

```bash
chmod +x scripts/generate-certs.sh
./scripts/generate-certs.sh
```

Génère `certs/localhost.crt` et `certs/localhost.key` (auto-signés).

### 4. Lancer la stack

```bash
docker compose up -d --build
```

### 5. Vérifier que tout fonctionne

```bash
# Attendre que les services soient prêts (30-60 secondes)
docker compose ps

# Health check de l'API
curl -sk https://localhost:18443/api/public/health
# → ok

# Connexion en tant que user1
curl -sk -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -d "client_id=task-api" \
  -d "grant_type=password" \
  -d "username=user1" \
  -d "password=password123"
# → { "access_token": "eyJ...", ... }

# Console admin Keycloak
# https://localhost:18443/admin/ → credentials admin / changeme
```

> L'option `-k` (curl) ou `--insecure` est nécessaire avec des certificats auto-signés.

---

## Services

| Service | URL | Description |
|---|---|---|
| API (HTTPS) | https://localhost:18443/api | Tous les endpoints via Nginx |
| Keycloak (HTTPS) | https://localhost:18443 | Auth + Admin Console |
| Nginx HTTP | http://localhost:18088 | Redirige vers HTTPS |
| Keycloak direct | http://localhost:18082 | HTTP direct (sans cert) |

**Ports internes** (accessibles entre conteneurs uniquement) :

| Service | Port interne |
|---|---|
| App (Spring Boot) | `app:8080` |
| Keycloak | `keycloak:8080` |
| PostgreSQL | `postgres:5432` |

---

## API

Documentation détaillée : [docs/api.md](docs/api.md)

| Méthode | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/public/health` | ❌ | Health check |
| POST | `/api/auth/register` | ❌ | Créer un compte |
| GET | `/api/tasks` | ✅ JWT | Lister les tâches |
| POST | `/api/tasks` | ✅ JWT | Créer une tâche |
| DELETE | `/api/tasks/{id}` | ✅ JWT (owner) | Supprimer sa tâche |
| DELETE | `/api/admin/tasks/{id}` | ✅ ADMIN | Supprimer toute tâche |

---

## Utilisateurs pré-configurés

| Utilisateur | Mot de passe | Rôles |
|---|---|---|
| `user1` | `password123` | USER |
| `admin1` | `admin123` | ADMIN, USER |

---

## Test rapide (curl)

```bash
# Variables
TOKEN=$(curl -sk -X POST https://localhost:18443/realms/Taks/protocol/openid-connect/token \
  -d "client_id=task-api" -d "grant_type=password" \
  -d "username=user1" -d "password=password123" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

# Créer une tâche
curl -sk -X POST https://localhost:18443/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Ma première tâche"}'

# Lister les tâches
curl -sk https://localhost:18443/api/tasks \
  -H "Authorization: Bearer $TOKEN"

# Nouvel utilisateur
curl -sk -X POST https://localhost:18443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"pass123"}'
```

---

## Dépannage

### Ports déjà utilisés

```bash
# Vérifier si les ports sont libres
sudo lsof -i :18443,18082,18088,18090,5432
```

Modifier les ports dans `docker-compose.yml` si nécessaire.

### Keycloak ne démarre pas

```bash
docker compose logs keycloak
# Erreur fréquente : base de données non prête → attendre 30s et redémarrer
docker compose restart keycloak
```

### L'API retourne 401

- Le token est-il expiré ? (durée par défaut : 1h)
- Le client `task-api` existe-t-il ? Vérifier dans la console admin Keycloak

### Certificat SSL non trouvé

```bash
./scripts/generate-certs.sh
# Vérifier que certs/localhost.crt et .key existent
```

---

## CI/CD

- **GitHub Actions** : Déclenché sur chaque `git push` vers `main`
  - Build Maven
  - Tests unitaires
  - OWASP Dependency Check
  - Build Docker
  - Scan Trivy
  - Tests Newman (Postman)
