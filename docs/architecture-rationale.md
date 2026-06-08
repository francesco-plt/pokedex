# Architecture Rationale

## Why hexagonal
- Keeps domain and use cases isolated from frameworks.
- Makes adapters replaceable (DB, HTTP, messaging).
- Supports incremental evolution from monolith to modular services.

## Why Spring Data JDBC
- Lower complexity than JPA/Hibernate for CRUD-style aggregates.
- Transparent SQL and explicit aggregate boundaries.
- Easier performance reasoning for backend teams.

## Transaction boundaries
- Transactions belong to application services (use-case implementations).
- Domain remains framework-free and side-effect free.

## Scalability path
- Split modules later (`domain`, `application`, `adapters`, `bootstrap`).
- Add outbox + CDC for reliable integration events.
- Add contract tests for external adapters.
