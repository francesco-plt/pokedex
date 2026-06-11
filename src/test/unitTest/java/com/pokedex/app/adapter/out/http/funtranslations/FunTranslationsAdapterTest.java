package com.pokedex.app.adapter.out.http.funtranslations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsRateLimitException;
import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsUnavailableException;
import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationContent;
import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationResponse;
import feign.Request;
import feign.RetryableException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class FunTranslationsAdapterTest {

    @Test
    void shouldTranslateDescriptionToYoda() {
        // Arrange
        FunTranslationsClient client = mock(FunTranslationsClient.class);
        FunTranslationsTranslationResponse response = FunTranslationsTranslationResponse.builder()
            .contents(FunTranslationsTranslationContent.builder().translated("Powerful you have become.").build())
            .build();
        when(client.translateYoda(any())).thenReturn(response);

        // Act
        FunTranslationsAdapter adapter = new FunTranslationsAdapter(client);
        var result = adapter.translateToYoda("You have become powerful.");

        // Assert
        assertThat(result).isPresent().contains("Powerful you have become.");
    }

    @Test
    void shouldReturnEmptyWhenRetryableExceptionThrown() {
        // Arrange
        FunTranslationsClient client = mock(FunTranslationsClient.class);
        Request request = Request.create(
            Request.HttpMethod.POST,
            "https://api.funtranslations.com/translate/shakespeare",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        RetryableException retryableException = new RetryableException(
            503,
            "Connection reset",
            Request.HttpMethod.POST,
            (Long) null,
            request
        );
        when(client.translateShakespeare(any())).thenThrow(retryableException);

        // Act
        FunTranslationsAdapter adapter = new FunTranslationsAdapter(client);

        // Assert
        assertThat(adapter.translateToShakespeare("Description")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenUnexpectedErrorOccurs() {
        // Arrange
        FunTranslationsClient client = mock(FunTranslationsClient.class);
        when(client.translateYoda(any())).thenThrow(new IllegalStateException("boom"));

        // Act
        FunTranslationsAdapter adapter = new FunTranslationsAdapter(client);

        // Assert
        assertThat(adapter.translateToYoda("Description")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenRateLimitExceptionThrown() {
        // Arrange
        FunTranslationsClient client = mock(FunTranslationsClient.class);
        when(client.translateShakespeare(any())).thenThrow(
            new FunTranslationsRateLimitException(
                "FunTranslations returned HTTP 429 while requesting 'shakespeare' translation. Retry after 20 seconds.",
                20
            )
        );

        // Act
        FunTranslationsAdapter adapter = new FunTranslationsAdapter(client);

        // Assert
        assertThat(adapter.translateToShakespeare("Description")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenServiceUnavailable() {
        // Arrange
        FunTranslationsClient client = mock(FunTranslationsClient.class);
        when(client.translateYoda(any())).thenThrow(
            new FunTranslationsUnavailableException(
                "FunTranslations returned HTTP 503 while requesting 'yoda' translation."
            )
        );

        // Act
        FunTranslationsAdapter adapter = new FunTranslationsAdapter(client);

        // Assert
        assertThat(adapter.translateToYoda("desc")).isEmpty();
    }
}
