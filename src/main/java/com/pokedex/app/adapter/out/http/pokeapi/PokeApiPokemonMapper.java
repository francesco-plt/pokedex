package com.pokedex.app.adapter.out.http.pokeapi;

import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonFlavorTextEntry;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonHabitat;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonSpecieResponse;
import com.pokedex.app.domain.Pokemon;

import java.util.Optional;

final class PokeApiPokemonMapper {

    static Pokemon fromSpecieToPokemon(PokeApiPokemonSpecieResponse pokeApiPokemonSpecieResponse) {
        return Pokemon.builder()
                .name(pokeApiPokemonSpecieResponse.name())
                .description(fetchDescription(pokeApiPokemonSpecieResponse))
                .habitat(Optional.ofNullable(pokeApiPokemonSpecieResponse.habitat())
                        .map(PokeApiPokemonHabitat::name)
                        .orElse(""))
                .isLegendary(pokeApiPokemonSpecieResponse.isLegendary())
                .build();
    }

    static private String fetchDescription(PokeApiPokemonSpecieResponse pokeApiPokemonSpecieResponse) {
        if (pokeApiPokemonSpecieResponse.flavorTextEntries() == null || pokeApiPokemonSpecieResponse.flavorTextEntries().isEmpty()) {
            return "";
        }
        return pokeApiPokemonSpecieResponse.flavorTextEntries().stream()
                .map(PokeApiPokemonFlavorTextEntry::flavorText)
                .findFirst()
                .orElse("");
    }
}
