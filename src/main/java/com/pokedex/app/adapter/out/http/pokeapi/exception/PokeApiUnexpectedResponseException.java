package com.pokedex.app.adapter.out.http.pokeapi.exception;

import java.io.Serial;

public final class PokeApiUnexpectedResponseException extends PokeApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PokeApiUnexpectedResponseException(String message) {
        super(message);
    }

    public PokeApiUnexpectedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
