# Architecture Rationale

## Why hexagonal
- Keeps domain and use cases isolated from frameworks.
- Makes adapters replaceable for outbound dependencies (PokeAPI and FunTranslations).
- Keeps translation and Pokemon-fetching rules testable without HTTP/runtime concerns.

## Why OpenFeign + explicit client configs
- Simple declarative HTTP clients with concise adapter code.
- Per-upstream configuration allows explicit timeouts and retry policy.
- Custom error decoders convert provider-specific HTTP errors into domain-friendly exceptions.

## Resilience and behavior choices
- PokeAPI failures are mapped to API-level `404` / `503` / `502` responses.
- Translation failures are intentionally non-fatal; API falls back to the original description.
- Translation language selection is domain-driven: legendary or cave uses Yoda, otherwise Shakespeare.

## Scalability path
- Add outbound caching for Pokemon species and translation responses.
- Add resilience patterns (bounded retries with backoff, circuit breakers).
- Add contract tests for external providers and populate `architectureTest` source set.
