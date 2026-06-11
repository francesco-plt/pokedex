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

        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/pikachu",
            PokemonResponse.class
        );

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
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"detail\":\"not found\"}")
        );

        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/pokemon/missingno",
            String.class
        );

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
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(503)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"detail\":\"temporarily unavailable\"}")
        );

        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/pokemon/slowpoke",
            String.class
        );

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
    void shouldReturnProblemDetailWhenPokeApiReturnsUnexpectedError() throws Exception {
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(418)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"detail\":\"teapot\"}")
        );

        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/pokemon/bulbasaur",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .isTrue();
        assertThat(response.getBody()).contains("\"title\":\"PokeAPI error\"");
        assertThat(response.getBody()).contains("\"status\":502");
        assertThat(response.getBody()).contains("bulbasaur");

        RecordedRequest request = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v2/pokemon-species/bulbasaur");
    }

    @Test
    void shouldReturnPokemonWithShakespeareTranslationForNonLegendaryNonCavePokemon() throws Exception {
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

        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/translated/bulbasaur",
            PokemonResponse.class
        );

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
    void shouldReturnPokemonWithYodaTranslationForCavePokemon() throws Exception {
        POKE_API.enqueue(
            new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "name": "zubat",
                      "habitat": {"name": "cave"},
                      "is_legendary": false,
                      "flavor_text_entries": [
                        {"flavor_text": "Forms colonies in perpetually dark places."}
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
                        "translated": "Colonies in dark places, forms it does."
                      }
                    }
                    """)
        );

        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/translated/zubat",
            PokemonResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("zubat");
        assertThat(response.getBody().description()).isEqualTo("Colonies in dark places, forms it does.");
        assertThat(response.getBody().habitat()).isEqualTo("cave");
        assertThat(response.getBody().isLegendary()).isFalse();

        RecordedRequest pokeApiRequest = POKE_API.takeRequest(1, TimeUnit.SECONDS);
        assertThat(pokeApiRequest).isNotNull();
        assertThat(pokeApiRequest.getPath()).isEqualTo("/api/v2/pokemon-species/zubat");

        RecordedRequest translationRequest = FUN_TRANSLATIONS.takeRequest(1, TimeUnit.SECONDS);
        assertThat(translationRequest).isNotNull();
        assertThat(translationRequest.getPath()).isEqualTo("/translate/yoda");
        assertThat(translationRequest.getMethod()).isEqualTo("POST");
        assertThat(translationRequest.getBody().readUtf8())
            .contains("\"text\":\"Forms colonies in perpetually dark places.\"");
    }

    @Test
    void shouldReturnOriginalDescriptionWhenTranslationServiceIsUnavailable() throws Exception {
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
                .setResponseCode(503)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "error": "service unavailable"
                    }
                    """)
        );

        ResponseEntity<PokemonResponse> response = restTemplate.getForEntity(
            "/api/v1/pokemon/translated/pikachu",
            PokemonResponse.class
        );

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
