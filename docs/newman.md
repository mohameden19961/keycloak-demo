# Newman

Exécuteur de collections Postman en ligne de commande. Utilisé pour les tests d'intégration API.

## Exécution

```bash
docker run --rm --network=host \
  -v $PWD/tests:/etc/newman \
  postman/newman:alpine \
  run /etc/newman/task-api-collection.json \
  --reporters cli,junit \
  --reporter-junit-export /etc/newman/results.xml
```

## Collection

**Fichier :** `tests/task-api-collection.json`

7 requêtes couvrant :

1. Health check public
2. Login (obtention token JWT)
3. Création d'une tâche
4. Liste des tâches
5. Suppression par le propriétaire
6. Accès refusé (tentative suppression tâche d'autrui)
7. Suppression admin

## Rapports

- **CLI** : Affichage dans la console
- **JUnit** : Export XML pour publication dans Jenkins / GitHub Actions
