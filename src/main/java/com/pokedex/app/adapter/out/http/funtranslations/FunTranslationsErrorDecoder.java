package com.pokedex.app.adapter.out.http.funtranslations;

import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsRateLimitException;
import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsUnavailableException;
import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsUnexpectedResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

final class FunTranslationsErrorDecoder implements ErrorDecoder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String translation = extractTranslationFromRequestPath(response);

        if (status == 429) {
            Integer retryAfter = extractRetryAfterSeconds(response);
            String message = retryAfter == null
                ? "FunTranslations returned HTTP 429 while requesting '%s' translation."
                    .formatted(translation)
                : "FunTranslations returned HTTP 429 while requesting '%s' translation. Retry after %d seconds."
                    .formatted(translation, retryAfter);
            return new FunTranslationsRateLimitException(message, retryAfter);
        }

        if (status >= 500) {
            return new FunTranslationsUnavailableException(
                "FunTranslations returned HTTP %d while requesting '%s' translation."
                    .formatted(status, translation)
            );
        }

        return new FunTranslationsUnexpectedResponseException(
            "FunTranslations returned unexpected HTTP %d while requesting '%s' translation."
                .formatted(status, translation)
        );
    }

    private static @Nullable Integer extractRetryAfterSeconds(Response response) {
        if (response.body() == null) {
            return null;
        }
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            JsonNode node = OBJECT_MAPPER.readTree(body).path("retry_after");
            return node.isInt() ? node.asInt() : null;
        } catch (IOException ex) {
            return null;
        }
    }

    private static String extractTranslationFromRequestPath(Response response) {
        return Optional.ofNullable(response.request())
            .map(Request::url)
            .map(URI::create)
            .map(URI::getPath)
            .map(path -> {
                int index = path.lastIndexOf('/');
                return index >= 0 ? path.substring(index + 1) : path;
            })
            .filter(value -> !value.isBlank())
            .orElse("unknown");
    }
}
