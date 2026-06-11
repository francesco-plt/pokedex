package com.pokedex.app.adapter.out.http.funtranslations.exception;

import java.io.Serial;

public final class FunTranslationsForbiddenException extends FunTranslationsException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FunTranslationsForbiddenException(String message) {
        super(message);
    }

    public FunTranslationsForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
