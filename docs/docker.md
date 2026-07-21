# Docker

Conteneurisation de la stack complète avec multi-stage build et orchestration Docker Compose.

## Dockerfile

Multi-stage build Java 21 :

| Stage | Image | Rôle |
|---|---|---|
| `build` | `eclipse-temurin:21-jdk-alpine` | Compilation Maven (`./mvnw clean package`) |
| Final | `eclipse-temurin:21-jre-alpine` | Exécution du JAR (`java -jar app.jar`) |

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Docker Compose

**Fichier :** `docker-compose.yml`

4 services interconnectés :

| Service | Image | Ports | Dépend de |
|---|---|---|---|
| `postgres` | `postgres:16` | `15432:5432` | — |
| `keycloak` | `quay.io/keycloak/keycloak:25.0` (production + import realm) | `18082:8080` | `postgres` |
| `app` | build local | `18090:8080` | `postgres`, `keycloak` |
| `nginx` | `nginx:alpine` | `18088:80`, `18443:443` | `app`, `keycloak` |

### Variables d'environnement

Tous les secrets sont chargés depuis `.env` (voir `.env.example`).

### Réseau

Les services communiquent entre eux via le réseau interne Docker (noms de container). L'API Keycloak JWT est résolue en interne : `http://keycloak:8080`.

## Commandes

```bash
# Démarrer la stack
docker compose up -d --build

# Voir les logs
docker compose logs -f

# Arrêter
docker compose down

# Rebuild un service spécifique
docker compose up -d --build app
```

## Volumes

| Volume | Montage | Persistance |
|---|---|---|
| `pgdata` | `/var/lib/postgresql/data` | Données PostgreSQL |
| `./certs` | `/etc/nginx/certs` | Certificats SSL |
| `./nginx.conf` | `/etc/nginx/conf.d/default.conf` | Configuration Nginx |
| `./realm-config` | `/opt/keycloak/data/import` | Import automatique du realm Keycloak |
