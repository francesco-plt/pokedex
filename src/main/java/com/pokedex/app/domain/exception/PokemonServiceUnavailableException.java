package com.pokedex.app.domain.exception;

import java.io.Serial;

public final class PokemonServiceUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PokemonServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
