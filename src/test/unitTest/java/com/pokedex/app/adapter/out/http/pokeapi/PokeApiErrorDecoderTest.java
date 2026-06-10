package com.pokedex.app.adapter.out.http.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiNotFoundException;
import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiUnavailableException;
import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiUnexpectedResponseException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PokeApiErrorDecoderTest {

    private static final String METHOD_KEY = "PokeApiClient#getPokemonSpecieByName";

    private final PokeApiErrorDecoder decoder = new PokeApiErrorDecoder();

    @Test
    void shouldMapNotFoundAsPokemonNotFoundException() {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatus(404, "https://pokeapi.co/api/v2/pokemon-species/pikachu")
        );

        // Assert
        assertThat(error).isInstanceOf(PokeApiNotFoundException.class);
        assertThat(((PokeApiNotFoundException) error).pokemonName()).isEqualTo("pikachu");
        assertThat(error.getMessage()).contains("pikachu");
    }

    @ParameterizedTest
    @ValueSource(ints = {429, 503})
    void shouldMapRateLimitAndServerErrorsAsUnavailable(int status) {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatus(status, "https://pokeapi.co/api/v2/pokemon-species/pikachu")
        );

        // Assert
        assertThat(error).isInstanceOf(PokeApiUnavailableException.class);
        assertThat(error.getMessage()).contains("HTTP %d".formatted(status));
        assertThat(error.getMessage()).contains("pikachu");
    }

    @Test
    void shouldMapOtherErrorsAsUnexpectedResponse() {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatus(418, "https://pokeapi.co/api/v2/pokemon-species/bulbasaur")
        );

        // Assert
        assertThat(error).isInstanceOf(PokeApiUnexpectedResponseException.class);
        assertThat(error.getMessage()).contains("HTTP 418");
        assertThat(error.getMessage()).contains("bulbasaur");
    }

    private static Response responseWithStatus(int status, String requestUrl) {
        Request request = Request.create(
            Request.HttpMethod.GET,
            requestUrl,
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );

        return Response.builder()
            .status(status)
            .reason("status %d".formatted(status))
            .request(request)
            .headers(Map.of())
            .build();
    }
}
