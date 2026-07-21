# Spring Boot 3

Framework Java pour la création d'API RESTful avec sécurité OAuth2 / JWT.

## Configuration

**Fichier :** `pom.xml` — Spring Boot 3.3 / Java 21

### Dépendances principales

| Dépendance | Rôle |
|---|---|
| `spring-boot-starter-webmvc` | API REST |
| `spring-boot-starter-data-jpa` | ORM / PostgreSQL |
| `spring-boot-starter-security` | Sécurité |
| `spring-boot-starter-oauth2-resource-server` | Validation JWT Keycloak |
| `postgresql` | Driver PostgreSQL |
| `lombok` | Réduction du boilerplate |

### Application

`src/main/resources/application.yml`

```yaml
server.port: 8080
spring.datasource.url: ${SPRING_DATASOURCE_URL}
spring.datasource.username: ${SPRING_DATASOURCE_USERNAME}
spring.datasource.password: ${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto: update
spring.security.oauth2.resourceserver.jwt.jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}
```

### Keycloak Admin (inscription)

`KeycloakAdminService.java` utilise l'Admin REST API de Keycloak pour créer des utilisateurs :

1. Obtient un token admin via le realm `master` (client `admin-cli`)
2. Crée l'utilisateur dans le realm `Taks` avec le rôle `USER` (par défaut)

### Sécurité

`SecurityConfig.java` :
- Endpoints `/api/public/**` accessibles sans auth
- Endpoints `/api/auth/**` accessibles sans auth (inscription)
- Endpoints `/api/admin/**` réservés au rôle `ADMIN`
- Tous les autres endpoints nécessitent un JWT valide
- Les rôles Keycloak sont extraits du claim `realm_access.roles`

### API

| Méthode | Endpoint | Contrôle d'accès |
|---|---|---|
| `GET` | `/api/public/health` | Public |
| `POST` | `/api/auth/register` | Public |
| `GET` | `/api/tasks` | Authentifié |
| `POST` | `/api/tasks` | Authentifié (owner = JWT subject) |
| `DELETE` | `/api/tasks/{id}` | Owner uniquement |
| `DELETE` | `/api/admin/tasks/{id}` | Rôle ADMIN |

## Build

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```
