package com.pokedex.app.adapter.out.http.pokeapi.exception;

import lombok.Getter;

import java.io.Serial;

public final class PokeApiNotFoundException extends PokeApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final String pokemonName;

    public PokeApiNotFoundException(String pokemonName, String message) {
        super(message);
        this.pokemonName = pokemonName;
    }

    public String pokemonName() {
        return pokemonName;
    }
}
