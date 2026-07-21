# Keycloak Demo

API REST sécurisée avec Spring Boot 3 + Keycloak + PostgreSQL, proxyfiée par Nginx, avec pipeline CI/CD Jenkins et GitHub Actions.

## Stack

| Technologie | Rôle |
|---|---|
| [Spring Boot 3](docs/spring-boot.md) | API RESTful avec OAuth2 / JWT |
| [Keycloak 25](docs/keycloak.md) | Serveur d'authentification et gestion des rôles |
| [PostgreSQL 16](docs/postgresql.md) | Base de données relationnelle |
| [Nginx](docs/nginx.md) | Reverse proxy HTTPS |
| [Jenkins](docs/jenkins.md) | Pipeline CI/CD automatisé |
| [SonarQube](docs/sonarqube.md) | Analyse statique de code |
| [OWASP Dependency Check](docs/owasp.md) | Scan des vulnérabilités des dépendances |
| [Trivy](docs/trivy.md) | Scan des vulnérabilités des containers |
| [Newman](docs/newman.md) | Tests d'intégration API Postman |

## Démarrage rapide

```bash
cp .env.example .env
# Éditer .env avec vos secrets
./scripts/generate-certs.sh    # Générer les certificats SSL
docker-compose up -d --build
```

### Services

| Service | URL | Accès |
|---|---|---|
| API | http://localhost:18090/api | Public + JWT |
| Keycloak | http://localhost:18082 | Admin `/admin` |
| Nginx HTTP | http://localhost:18088 | Redirection HTTPS |
| Nginx HTTPS | https://localhost:18443 | Reverse proxy |

## API

| Méthode | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/public/health` | Aucune | Health check |
| GET | `/api/tasks` | JWT | Lister les tâches |
| POST | `/api/tasks` | JWT | Créer une tâche |
| DELETE | `/api/tasks/{id}` | JWT (owner) | Supprimer sa tâche |
| DELETE | `/api/admin/tasks/{id}` | ADMIN | Supprimer toute tâche |

## CI/CD

- **Jenkins** : Pipeline local avec Newman → SonarQube → OWASP → Trivy
- **GitHub Actions** : Build Maven + Tests + OWASP + Trivy sur chaque push

## Secrets

Copier `.env.example` vers `.env` et renseigner :

```env
POSTGRES_PASSWORD=...
KC_DB_PASSWORD=...
KEYCLOAK_ADMIN_PASSWORD=...
SPRING_DATASOURCE_PASSWORD=...
SONAR_TOKEN=...
SONAR_HOST_URL=...
NVD_API_KEY=...
```
