package com.pokedex.app.adapter.out.http.funtranslations.exception;

import java.io.Serial;

public class FunTranslationsUnavailableException extends FunTranslationsException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FunTranslationsUnavailableException(String message) {
        super(message);
    }

    public FunTranslationsUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
