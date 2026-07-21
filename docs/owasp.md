# OWASP Dependency Check

Outil qui identifie les vulnérabilités connues (CVE) dans les dépendances Maven.

## Configuration

**Plugin Maven :** `dependency-check-maven` 10.0.4

```xml
<configuration>
  <failBuildOnCVSS>11</failBuildOnCVSS>
  <format>XML</format>
  <failOnError>false</failOnError>
  <nvdApiKey>${nvdApiKey}</nvdApiKey>
  <connectionTimeout>30000</connectionTimeout>
  <nvdValidForHours>48</nvdValidForHours>
</configuration>
```

### Exécution

```bash
./mvnw org.owasp:dependency-check-maven:check -DnvdApiKey=$NVD_API_KEY
```

## NVD API Key

Une clé NVD API est **obligatoire** pour éviter un téléchargement de 30+ min (367 922 enregistrements).

1. S'inscrire sur https://nvd.nist.gov/developers/request-an-api-key
2. Copier la clé UUID dans `NVD_API_KEY` (`.env`)
3. Le cache est valide 48h (`nvdValidForHours=48`)

## Résultat

Le rapport XML est publié dans Jenkins via le plugin OWASP Dependency Check Publisher.
