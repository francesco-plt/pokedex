package com.pokedex.app.domain.exception;

import java.io.Serial;

public final class PokemonFetchException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PokemonFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
