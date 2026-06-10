package com.pokedex.app.adapter.out.http;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pokedex.app.adapter.out.http.pokeapi.PokeApiClient;
import com.pokedex.app.adapter.out.http.pokeapi.PokeApiAdapter;
import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiUnexpectedResponseException;
import com.pokedex.app.domain.exception.PokemonFetchException;
import com.pokedex.app.domain.exception.PokemonServiceUnavailableException;
import feign.Request;
import feign.RetryableException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class PokeApiAdapterTest {

    @Test
    void shouldWrapRetryableExceptionAsUnavailable() {
        // Arrange
        PokeApiClient pokeApiClient = mock(PokeApiClient.class);
        Request request = Request.create(
            Request.HttpMethod.GET,
            "https://pokeapi.co/api/v2/pokemon/pikachu",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        RetryableException retryableException = new RetryableException(
            503,
            "Connection reset",
            Request.HttpMethod.GET,
            (Long) null,
            request
        );

        when(pokeApiClient.getPokemonSpecieByName("pikachu")).thenThrow(retryableException);

        // Act
        PokeApiAdapter gateway = new PokeApiAdapter(pokeApiClient);

        // Assert
        assertThatThrownBy(() -> gateway.getPokemonByName("pikachu"))
            .isInstanceOf(PokemonServiceUnavailableException.class)
            .hasMessageContaining("pikachu");
    }

    @Test
    void shouldWrapUnexpectedErrorsAsPokemonFetchException() {
        // Arrange
        PokeApiClient pokeApiClient = mock(PokeApiClient.class);
        when(pokeApiClient.getPokemonSpecieByName("pikachu")).thenThrow(
            new PokeApiUnexpectedResponseException("Unexpected response")
        );

        // Act
        PokeApiAdapter gateway = new PokeApiAdapter(pokeApiClient);

        // Assert
        assertThatThrownBy(() -> gateway.getPokemonByName("pikachu"))
            .isInstanceOf(PokemonFetchException.class)
            .hasMessageContaining("Unexpected error while fetching pokemon")
            .hasCauseInstanceOf(PokeApiUnexpectedResponseException.class);
    }
}
