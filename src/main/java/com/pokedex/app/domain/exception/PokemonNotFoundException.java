package com.pokedex.app.domain.exception;

import java.io.Serial;

public final class PokemonNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String pokemonName;

    public PokemonNotFoundException(String pokemonName) {
        super("Pokemon '%s' not found.".formatted(pokemonName));
        this.pokemonName = pokemonName;
    }

    public String pokemonName() {
        return pokemonName;
    }
}
