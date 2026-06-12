# Architecture Rationale

## Why hexagonal
- Keeps domain and use cases isolated from frameworks.
- Makes adapters replaceable (DB, HTTP, messaging).
- Supports incremental evolution from monolith to modular services.

## Why Spring Data JDBC
- Lower complexity than JPA/Hibernate for CRUD-style aggregates.
- Transparent SQL and explicit aggregate boundaries.
- Easier performance reasoning for backend developers.

## Scalability path
- Add contract tests for external adapters (see `architectureTests`)
- Improve abstraction and avoid injecting concrete implementations directly for services at `com.pokedex.app.service`