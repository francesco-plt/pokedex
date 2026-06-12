package com.pokedex.app.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.pokedex.app.adapter.in.web.model.PokemonResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonControllerIntegrationTest {

    private static final MockWebServer POKE_API = startServer();
    private static final MockWebServer FUN_TRANSLATIONS = startServer();

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void pokeApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.cloud.openfeign.client.config.poke-api-client.url",
            () -> POKE_API.url("/api/v2/").toString()
        );
        registry.add(
            "spring.cloud.openfeign.client.config.funtranslations-client.url",
            () -> FUN_TRANSLATIONS.url("/").toString()
        );
    }

    @AfterAll
    static void shutdownServers() throws IOException {
        try {
            POKE_API.shutdown();
        } finally {
            FUN_TRANSLATIONS.shutdown();
        }
    }

    @Test
    void shouldReturnPokemonFromPokeApi() throws Exception {
        // Arrange
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "name": "pikachu",
                      "habitat": {"name": "forest"},
                      "is_legendary": false,
                      "flavor_text_entries": [
                        {"flavor_text": "electric\\fmouse"}
                      ]
                    }
                    """)
        );

        // Act
        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/pikachu",
            PokemonResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("pikachu");
        assertThat(response.getBody().description()).isEqualTo("electric\nmouse");
        assertThat(response.getBody().habitat()).isEqualTo("forest");
        assertThat(response.getBody().isLegendary()).isFalse();

        RecordedRequest request = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v2/pokemon-species/pikachu");
    }

    @Test
    void shouldReturnProblemDetailWhenPokemonIsMissing() throws Exception {
        // Arrange
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"detail\":\"not found\"}")
        );

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/pokemon/missingno",
            String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .isTrue();
        assertThat(response.getBody()).contains("\"title\":\"Pokemon not found\"");
        assertThat(response.getBody()).contains("\"status\":404");
        assertThat(response.getBody()).contains("missingno");

        RecordedRequest request = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v2/pokemon-species/missingno");
    }

    @Test
    void shouldReturnProblemDetailWhenPokeApiReturnsServerError() throws Exception {
        // Arrange
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(503)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"detail\":\"temporarily unavailable\"}")
        );

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/pokemon/slowpoke",
            String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .isTrue();
        assertThat(response.getBody()).contains("\"title\":\"PokeAPI unavailable\"");
        assertThat(response.getBody()).contains("\"status\":503");
        assertThat(response.getBody()).contains("slowpoke");

        RecordedRequest request = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v2/pokemon-species/slowpoke");
    }

    @Test
    void shouldReturnPokemonWithShakespeareTranslationForNonLegendaryNonCavePokemon() throws Exception {
        // Arrange
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "name": "bulbasaur",
                      "habitat": {"name": "forest"},
                      "is_legendary": false,
                      "flavor_text_entries": [
                        {"flavor_text": "A strange seed was planted on its back."}
                      ]
                    }
                    """)
        );
        FUN_TRANSLATIONS.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "contents": {
                        "translated": "A strange seed wast planted on its back."
                      }
                    }
                    """)
        );

        // Act
        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/translated/bulbasaur",
            PokemonResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("bulbasaur");
        assertThat(response.getBody().description()).isEqualTo("A strange seed wast planted on its back.");
        assertThat(response.getBody().habitat()).isEqualTo("forest");
        assertThat(response.getBody().isLegendary()).isFalse();

        RecordedRequest pokeApiRequest = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(pokeApiRequest).isNotNull();
        assertThat(pokeApiRequest.getPath()).isEqualTo("/api/v2/pokemon-species/bulbasaur");

        RecordedRequest translationRequest = FUN_TRANSLATIONS.takeRequest(1, TimeUnit.SECONDS);
        assertThat(translationRequest).isNotNull();
        assertThat(translationRequest.getPath()).isEqualTo("/translate/shakespeare");
        assertThat(translationRequest.getMethod()).isEqualTo("POST");
        assertThat(translationRequest.getBody().readUtf8())
            .contains("\"text\":\"A strange seed was planted on its back.\"");
    }

    @Test
    void shouldReturnPokemonWithYodaTranslationForLegendaryPokemon() throws Exception {
        // Arrange
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "name": "mewtwo",
                      "habitat": {"name": "rare"},
                      "is_legendary": true,
                      "flavor_text_entries": [
                        {"flavor_text": "It was created by\\na scientist after\\nyears of horrific\\ngene splicing and\\nDNA engineering\\nexperiments."}
                      ]
                    }
                    """)
        );
        FUN_TRANSLATIONS.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "contents": {
                        "translated": "It was made by\\na one of powerful knowledge after\\nyears of terrible\\ngene fusing and\\nDNA crafting\\ntrials, young one."
                      }
                    }
                    """)
        );

        // Act
        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/translated/mewtwo",
            PokemonResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("mewtwo");
        assertThat(response.getBody().description()).isEqualTo("It was made by\na one of powerful knowledge after\nyears of terrible\ngene fusing and\nDNA crafting\ntrials, young one.");
        assertThat(response.getBody().habitat()).isEqualTo("rare");
        assertThat(response.getBody().isLegendary()).isTrue();

        RecordedRequest pokeApiRequest = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(pokeApiRequest).isNotNull();
        assertThat(pokeApiRequest.getPath()).isEqualTo("/api/v2/pokemon-species/mewtwo");

        RecordedRequest translationRequest = FUN_TRANSLATIONS.takeRequest(1, TimeUnit.SECONDS);
        assertThat(translationRequest).isNotNull();
        assertThat(translationRequest.getPath()).isEqualTo("/translate/yoda");
        assertThat(translationRequest.getMethod()).isEqualTo("POST");
        assertThat(translationRequest.getBody().readUtf8())
            .contains("\"text\":\"It was created by\\na scientist after\\nyears of horrific\\ngene splicing and\\nDNA engineering\\nexperiments.");
    }

    @ParameterizedTest
    @ValueSource(ints = {429, 503})
    void shouldReturnOriginalDescriptionWhenTranslationServiceIsUnavailable(int funTranslationsResponseStatus) throws Exception {
        // Arrange
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "name": "pikachu",
                      "habitat": {"name": "forest"},
                      "is_legendary": false,
                      "flavor_text_entries": [
                        {"flavor_text": "Electric type pokemon."}
                      ]
                    }
                    """)
        );
        FUN_TRANSLATIONS.enqueue(
            new MockResponse()
                .setResponseCode(funTranslationsResponseStatus)
                .addHeader("Content-Type", "application/json")
        );

        // Act
        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/translated/pikachu",
            PokemonResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("pikachu");
        assertThat(response.getBody().description()).isEqualTo("Electric type pokemon.");
        assertThat(response.getBody().habitat()).isEqualTo("forest");
        assertThat(response.getBody().isLegendary()).isFalse();

        RecordedRequest pokeApiRequest = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(pokeApiRequest).isNotNull();
        assertThat(pokeApiRequest.getPath()).isEqualTo("/api/v2/pokemon-species/pikachu");

        RecordedRequest translationRequest = FUN_TRANSLATIONS.takeRequest(1, TimeUnit.SECONDS);
        assertThat(translationRequest).isNotNull();
        assertThat(translationRequest.getPath()).isEqualTo("/translate/shakespeare");
        assertThat(translationRequest.getMethod()).isEqualTo("POST");
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to start MockWebServer", ex);
        }
    }
}
