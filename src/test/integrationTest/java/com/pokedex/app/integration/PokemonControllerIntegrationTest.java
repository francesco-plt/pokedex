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

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void pokeApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.cloud.openfeign.client.config.poke-api-client.url",
            () -> POKE_API.url("/api/v2/").toString()
        );
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        POKE_API.shutdown();
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
