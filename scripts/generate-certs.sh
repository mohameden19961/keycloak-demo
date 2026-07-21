#!/bin/sh
mkdir -p certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout certs/localhost.key \
  -out certs/localhost.crt \
  -subj "/CN=localhost/O=Keycloak Demo/C=MA" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
echo "Certificates generated in certs/"
