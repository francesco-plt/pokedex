package com.pokedex.app.adapter.out.http.funtranslations.exception;

import java.io.Serial;

public final class FunTranslationsUnexpectedResponseException extends FunTranslationsException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FunTranslationsUnexpectedResponseException(String message) {
        super(message);
    }

    public FunTranslationsUnexpectedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
