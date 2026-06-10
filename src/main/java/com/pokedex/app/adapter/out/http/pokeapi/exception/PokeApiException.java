package com.pokedex.app.adapter.out.http.pokeapi.exception;

import java.io.Serial;

public class PokeApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PokeApiException(String message) {
        super(message);
    }

    public PokeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
