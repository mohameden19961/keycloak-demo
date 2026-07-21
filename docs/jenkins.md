# Jenkins

Pipeline CI/CD automatisé pour l'analyse de code et la sécurité.

## Pipeline (`Jenkinsfile`)

6 stages exécutés séquentiellement :

| Stage | Description |
|---|---|
| **Start Stack** | `docker-compose up -d --build` + healthcheck dynamique |
| **Newman Tests** | 7 tests API via Postman/Newman |
| **SonarQube Analysis** | Analyse statique avec SonarQube |
| **OWASP Dependency Check** | Scan des vulnérabilités des dépendances Maven |
| **Trivy Scan** | Scan des vulnérabilités de l'image Docker |
| **Post Actions** | `docker-compose down`, publication JUnit + OWASP |

### Prérequis Jenkins

- Plugin **SonarQube Scanner**
- Plugin **OWASP Dependency Check**
- Plugin **JUnit**
- Docker CLI avec socket monté
- `docker-compose` v2
- `trivy` installé dans le container

### Exécution

```bash
curl -X POST http://localhost:18080/job/keycloak-demo-tests/build \
  -u <user>:<token> \
  -H "Jenkins-Crumb: <crumb>"
```

## Architecture

Le pipeline tourne dans un container Jenkins avec accès au socket Docker, lui permettant de gérer la stack complète (build, test, analyse).
