# Spring Boot Template (Minimal)

Minimal Java 25 + Spring Boot template exposing only a hello-world endpoint.

## Endpoint
- `GET /api/v1/hello_world` -> `hello_world`

## Run locally
- `./gradlew bootRun`
- `curl http://localhost:8080/api/v1/hello_world`

## Run with Docker
- `make up`
- `curl http://localhost:8080/api/v1/hello_world`
- `make down`

## Build and test
- `./gradlew clean check`
