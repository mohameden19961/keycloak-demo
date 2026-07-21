# Trivy

Scanner de vulnérabilités pour images Docker, fichiers systèmes, dépôts Git et configurations IaC.

## Utilisation

Scan de l'image Docker avec seuil HIGH et CRITICAL :

```bash
trivy image --severity HIGH,CRITICAL --exit-code 0 keycloak-demo-app
```

### Flag `--exit-code 0`

Le pipeline continue même si des vulnérabilités sont détectées (mode informatif). Pour bloquer le build, utiliser `--exit-code 1`.

## Intégration Jenkins

Le stage Trivy s'exécute après OWASP Dependency Check, sur l'image buildée par `docker-compose up --build`.

## Installation

```bash
# Linux
curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh

# Docker
docker run aquasec/trivy:latest image keycloak-demo-app
```
