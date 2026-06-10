package com.pokedex.app.adapter.in.web;

import com.pokedex.app.adapter.in.web.model.PokemonResponse;
import com.pokedex.app.domain.Pokemon;

final class PokemonResponseMapper {

    static PokemonResponse toPokemonResponse(Pokemon pokemon) {
        return PokemonResponse.builder()
                .name(pokemon.name())
                .description(pokemon.description())
                .habitat(pokemon.habitat())
                .isLegendary(pokemon.isLegendary())
                .build();
    }
}
