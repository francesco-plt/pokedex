package com.pokedex.app.adapter.out.http.pokeapi.exception;

import java.io.Serial;

public final class PokeApiUnavailableException extends PokeApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PokeApiUnavailableException(String message) {
        super(message);
    }

    public PokeApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
