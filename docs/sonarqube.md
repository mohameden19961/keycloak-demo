# SonarQube

Plateforme d'analyse statique de code. Détecte les bugs, vulnérabilités, code smells et problèmes de maintenabilité.

## Configuration

**Plugin Maven :** `sonar-maven-plugin` 4.0.0.4121

### Exécution

```bash
./mvnw compile sonar:sonar \
  -Dsonar.projectKey=keycloak-demo \
  -Dsonar.host.url=$SONAR_HOST_URL \
  -Dsonar.login=$SONAR_TOKEN
```

### Variables d'environnement

| Variable | Description |
|---|---|
| `SONAR_HOST_URL` | URL du serveur SonarQube (ex: `http://172.17.0.1:19000`) |
| `SONAR_TOKEN` | Token d'authentification généré dans SonarQube |

## Intégration Jenkins

Le pipeline Jenkins exécute l'analyse SonarQube après les tests Newman. Les résultats sont disponibles sur le dashboard SonarQube.

## Générer un token

1. Accéder à SonarQube → User → My Account → Security
2. Générer un token
3. Copier dans `SONAR_TOKEN` (`.env` ou variable Jenkins)
