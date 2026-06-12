# Pokedex API

Spring Boot service that fetches Pokemon species data from PokeAPI and exposes:
- raw Pokemon details
- translated descriptions (Yoda/Shakespeare) via FunTranslations

The codebase follows a ports-and-adapters (hexagonal) structure.

## Endpoints
- `GET /hello_world` -> `"Hello World!"`
- `GET /api/v1/pokemon/{name}` -> Pokemon details from PokeAPI
- `GET /api/v1/pokemon/translated/{name}` -> Pokemon details with translated description

Translation rule:
- Use `yoda` when Pokemon is legendary or habitat is `cave`
- Otherwise use `shakespeare`
- If translation provider fails, the API returns the original description

## Example response
```json
{
  "name": "pikachu",
  "description": "When several of these POK\u00e9MON gather, their electricity could build and cause lightning storms.",
  "habitat": "forest",
  "isLegendary": false
}
```

## Error model
Errors are returned as `application/problem+json` (`ProblemDetail`):
- `404` when Pokemon is not found
- `503` when PokeAPI is unavailable
- `502` for unexpected upstream API provider errors

## Project structure
```text
src/main/java/com/pokedex/app
  adapter/in/web                 # REST controllers + exception mapping
  adapter/out/http/pokeapi       # PokeAPI Feign client + adapter
  adapter/out/http/funtranslations# FunTranslations Feign client + adapter
  domain                          # Core domain model + domain exceptions/constants
  port/out                        # Outbound ports used by services
  service                         # Application services/use cases

src/test
  unitTest/java                   # Fast unit tests
  integrationTest/java            # Spring Boot integration tests (MockWebServer)
  architectureTest/java           # Reserved source set for architecture tests
```

Additional context:
- [Architecture rationale](docs/architecture-rationale.md)

## Run locally
Requirements:
- git
- Java 25 
- Gradle


Start app:
```bash
./gradlew bootRun
```

Smoke test:
```bash
http :8080/hello_world
http :8080/api/v1/pokemon/pikachu
http :8080/api/v1/pokemon/translated/zubat
```

Verify:
```bash
./gradlew clean check
```

## Run with Docker
```bash
make up
http :8080/api/v1/pokemon/pikachu
make down
```

Notes:
- `docker-compose.yml` publishes `8080` (HTTP) and `5005` (JVM remote debug)

## Build, test, and quality
Gradle tasks:
- `./gradlew clean assemble`
- `./gradlew test`
- `./gradlew integrationTest`
- `./gradlew architectureTest`
- `./gradlew check` (includes tests + `spotlessCheck`)
- `./gradlew spotlessApply`

Make aliases:
- `make build`, `make test`, `make it`, `make arch`, `make check`, `make spotless`

## Configuration
Default external base URLs are in [`src/main/resources/application.yml`](src/main/resources/application.yml):
- `spring.cloud.openfeign.client.config.poke-api-client.url=https://pokeapi.co/api/v2`
- `spring.cloud.openfeign.client.config.funtranslations-client.url=https://api.funtranslations.mercxry.me/v1`

## Production design notes (what I would change)
The current choices are fine for a coding challenge and keep the design simple. For production, I would change a few things:

1. Resilience policy around outbound calls.
- Application-level / Redis based caching with TTL for upstream requests (e.g., Pokemon details, translation results)
- Exponential backoff and bounded retries with jitter
- Circuit breakers
- Per-upstream SLO/error budgets
2. API hardening.
- Add API authentication or implement a gateway in front of the service to manage:
  - Per-client rate limits with buckets
  - Request-level audit/trace correlation.
3. Deployment/runtime concerns.
- Add full observability (metrics/traces), operational dashboards, and contract tests for external APIs.
