package com.pokedex.app.adapter.in.web;

import com.pokedex.app.domain.exception.PokemonFetchException;
import com.pokedex.app.domain.exception.PokemonNotFoundException;
import com.pokedex.app.domain.exception.PokemonServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PokedexExceptionHandlerTest {

    private final PokedexExceptionHandler handler = new PokedexExceptionHandler();

    @Test
    void handlePokemonNotFound_shouldReturnNotFoundWithProperDetails() {
        HttpServletRequest request = createMockRequest("/api/pokemon/unknown");
        PokemonNotFoundException exception = new PokemonNotFoundException("Pikachu");

        var response = handler.handlePokemonNotFound(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getTitle()).isEqualTo("Pokemon not found");
        assertThat(response.getBody().getDetail()).isEqualTo("Pokemon 'Pikachu' not found.");
        assertThat(response.getBody().getInstance()).isNotNull();
    }

    @Test
    void handlePokemonNotFound_usesExceptionMessage() {
        HttpServletRequest request = createMockRequest("/api/pokemon/unknown");
        PokemonNotFoundException exception = new PokemonNotFoundException("Charizard");

        var response = handler.handlePokemonNotFound(exception, request);

        assertThat(response.getBody().getDetail()).isEqualTo("Pokemon 'Charizard' not found.");
    }

    @Test
    void handlePokemonNotFound_usesFallbackDetail_whenMessageIsNull() {
        HttpServletRequest request = createMockRequest("/api/pokemon/unknown");
        PokemonNotFoundException exception = new PokemonNotFoundException(null);

        var response = handler.handlePokemonNotFound(exception, request);

        assertThat(response.getBody().getDetail()).isEqualTo("Pokemon 'null' not found.");
    }

    @Test
    void handlePokeApiUnavailable_shouldReturnServiceUnavailableWithProperDetails() {
        HttpServletRequest request = createMockRequest("/api/pokemon/bulbasaur");
        PokemonServiceUnavailableException exception = new PokemonServiceUnavailableException(
                "Service temporarily unavailable",
                new RuntimeException("Connection timeout")
        );

        var response = handler.handlePokeApiUnavailable(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().getTitle()).isEqualTo("PokeAPI unavailable");
        assertThat(response.getBody().getDetail()).isEqualTo("Service temporarily unavailable");
        assertThat(response.getBody().getInstance()).isNotNull();
    }

    @Test
    void handlePokeApiUnavailable_usesFallbackDetail_whenMessageIsNull() {
        HttpServletRequest request = createMockRequest("/api/pokemon/bulbasaur");
        PokemonServiceUnavailableException exception = new PokemonServiceUnavailableException(
                null,
                new RuntimeException("Connection timeout")
        );

        var response = handler.handlePokeApiUnavailable(exception, request);

        assertThat(response.getBody().getDetail()).isEqualTo("PokeAPI is temporarily unavailable.");
    }

    @Test
    void handlePokemonFetchError_shouldReturnBadGatewayWithProperDetails() {
        HttpServletRequest request = createMockRequest("/api/pokemon/charmander");
        PokemonFetchException exception = new PokemonFetchException(
                "Failed to fetch pokemon",
                new RuntimeException("Network error")
        );

        var response = handler.handlePokemonFetchError(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody().getTitle()).isEqualTo("PokeAPI error");
        assertThat(response.getBody().getDetail()).isEqualTo("Failed to fetch pokemon");
        assertThat(response.getBody().getInstance()).isNotNull();
    }

    @Test
    void handlePokemonFetchError_usesFallbackDetail_whenMessageIsNull() {
        HttpServletRequest request = createMockRequest("/api/pokemon/charmander");
        PokemonFetchException exception = new PokemonFetchException(
                null,
                new RuntimeException("Network error")
        );

        var response = handler.handlePokemonFetchError(exception, request);

        assertThat(response.getBody().getDetail()).isEqualTo("PokeAPI returned an unexpected error.");
    }

    @Test
    void handlePokemonNotFound_instanceUriIncludesRequestPath() {
        String requestPath = "/api/pokemon/nonexistent";
        HttpServletRequest request = createMockRequest(requestPath);
        PokemonNotFoundException exception = new PokemonNotFoundException("Not found");

        var response = handler.handlePokemonNotFound(exception, request);

        assertThat(response.getBody().getInstance().toString()).isEqualTo(requestPath);
    }

    private static HttpServletRequest createMockRequest(String requestUri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(requestUri);
        return request;
    }
}
