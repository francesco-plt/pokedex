package com.pokedex.app.adapter.out.http.funtranslations.exception;

import java.io.Serial;

public class FunTranslationsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FunTranslationsException(String message) {
        super(message);
    }

    public FunTranslationsException(String message, Throwable cause) {
        super(message, cause);
    }
}
