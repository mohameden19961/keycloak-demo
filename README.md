# Keycloak Demo

API REST sécurisée avec Spring Boot 3 + Keycloak + PostgreSQL, proxyfiée par Nginx, avec pipeline CI/CD GitHub Actions.

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
| [Docker](docs/docker.md) | Conteneurisation multi-stage + Docker Compose |
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
| API (HTTPS) | https://localhost:18443/api | Proxy Nginx |
| Keycloak Admin | https://localhost:18443/admin | Console d'admin |
| Nginx HTTP | http://localhost:18088 | Redirection vers HTTPS |

## API

Documentation complète : [docs/api.md](docs/api.md)

| Méthode | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/public/health` | Aucune | Health check |
| POST | `/api/auth/register` | Aucune | Créer un compte utilisateur |
| GET | `/api/tasks` | JWT | Lister les tâches |
| POST | `/api/tasks` | JWT | Créer une tâche |
| DELETE | `/api/tasks/{id}` | JWT (owner) | Supprimer sa tâche |
| DELETE | `/api/admin/tasks/{id}` | ADMIN | Supprimer toute tâche |

## Utilisateurs pré-configurés

| Utilisateur | Mot de passe | Rôles |
|---|---|---|
| `user1` | `password123` | USER |
| `admin1` | `admin123` | ADMIN, USER |

## CI/CD

- **GitHub Actions** : Build Maven + Tests + OWASP Dependency Check + Trivy + Newman sur chaque push

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
