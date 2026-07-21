# PostgreSQL 16

Base de données relationnelle. Ce projet utilise une instance PostgreSQL unique avec deux bases :

| Base | Usage |
|---|---|
| `keycloak_db` | Sessions, utilisateurs et configuration Keycloak |
| `app_db` | Données de l'application (tâches) |

## Configuration Docker

**Image :** `postgres:16`

```yaml
environment:
  POSTGRES_USER: ${POSTGRES_USER}
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
volumes:
  - pgdata:/var/lib/postgresql/data
  - ./init-multi-db.sh:/docker-entrypoint-initdb.d/init-multi-db.sh
```

## Initialisation

Le script `init-multi-db.sh` crée les deux bases au démarrage du container.

## Accès externe

```bash
psql -h localhost -p 15432 -U abdy -d app_db
```
