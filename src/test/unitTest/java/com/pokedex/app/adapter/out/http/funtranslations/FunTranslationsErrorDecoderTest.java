package com.pokedex.app.adapter.out.http.funtranslations;

import static org.assertj.core.api.Assertions.assertThat;

import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsRateLimitException;
import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsUnavailableException;
import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsUnexpectedResponseException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FunTranslationsErrorDecoderTest {

    private static final String METHOD_KEY = "FunTranslationsClient#translateYoda";

    private final FunTranslationsErrorDecoder decoder = new FunTranslationsErrorDecoder();

    @ParameterizedTest
    @ValueSource(ints = {429, 503})
    void shouldMapRateLimitAndServerErrorsAsUnavailable(int status) {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatus(status, "https://api.funtranslations.com/translate/yoda")
        );

        // Assert
        assertThat(error).isInstanceOf(
            status == 429 ? FunTranslationsRateLimitException.class : FunTranslationsUnavailableException.class
        );
        assertThat(error.getMessage()).contains("HTTP %d".formatted(status));
        assertThat(error.getMessage()).contains("yoda");
    }

    @Test
    void shouldExtractRetryAfterFromResponseBodyFor429() {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatusAndBody(
                429,
                "https://api.funtranslations.com/translate/yoda",
                "{\"retry_after\":37}"
            )
        );

        // Assert
        assertThat(error).isInstanceOf(FunTranslationsRateLimitException.class);
        FunTranslationsRateLimitException rateLimitException = (FunTranslationsRateLimitException) error;
        assertThat(rateLimitException.retryAfterSeconds()).isEqualTo(37);
        assertThat(rateLimitException.getMessage()).contains("Retry after 37 seconds");
    }

    @Test
    void shouldMapOtherErrorsAsUnexpectedResponse() {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatus(400, "https://api.funtranslations.com/translate/shakespeare")
        );

        // Assert
        assertThat(error).isInstanceOf(FunTranslationsUnexpectedResponseException.class);
        assertThat(error.getMessage()).contains("HTTP 400");
        assertThat(error.getMessage()).contains("shakespeare");
    }

    @Test
    void shouldMapTimeoutAsUnexpectedResponse() {
        // Arrange, Act
        Exception error = decoder.decode(
            METHOD_KEY,
            responseWithStatus(408, "https://api.funtranslations.com/translate/yoda")
        );

        // Assert
        assertThat(error).isInstanceOf(FunTranslationsUnexpectedResponseException.class);
        assertThat(error.getMessage()).contains("HTTP 408");
        assertThat(error.getMessage()).contains("yoda");
    }

    private static Response responseWithStatus(int status, String requestUrl) {
        Request request = Request.create(
            Request.HttpMethod.POST,
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

    private static Response responseWithStatusAndBody(int status, String requestUrl, String body) {
        Request request = Request.create(
            Request.HttpMethod.POST,
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
            .body(body, StandardCharsets.UTF_8)
            .build();
    }

}
