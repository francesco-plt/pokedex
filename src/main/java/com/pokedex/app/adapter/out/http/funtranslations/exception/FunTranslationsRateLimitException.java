package com.pokedex.app.adapter.out.http.funtranslations.exception;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

public final class FunTranslationsRateLimitException extends FunTranslationsUnavailableException {

    @Serial
    private static final long serialVersionUID = 1L;

    @Nullable
    private final Integer retryAfterSeconds;

    public FunTranslationsRateLimitException(String message, @Nullable Integer retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    @Nullable
    public Integer retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
