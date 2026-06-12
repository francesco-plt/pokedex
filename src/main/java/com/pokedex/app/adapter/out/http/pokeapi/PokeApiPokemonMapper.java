package com.pokedex.app.adapter.out.http.pokeapi;

import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonFlavorTextEntry;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonHabitat;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonLanguage;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonSpecieResponse;
import com.pokedex.app.domain.Pokemon;

import java.util.List;
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

    private static String fetchDescription(PokeApiPokemonSpecieResponse pokeApiPokemonSpecieResponse) {
        List<PokeApiPokemonFlavorTextEntry> flavorTextEntries = pokeApiPokemonSpecieResponse.flavorTextEntries();
        if (flavorTextEntries == null || flavorTextEntries.isEmpty()) {
            return "";
        }

        return flavorTextEntries.stream()
                .filter(PokeApiPokemonMapper::isEnglishEntry)
                .map(PokeApiPokemonFlavorTextEntry::flavorText)
                .findFirst()
                .orElse("");
    }

    private static boolean isEnglishEntry(PokeApiPokemonFlavorTextEntry entry) {
        return Optional.ofNullable(entry.language())
                .map(PokeApiPokemonLanguage::name)
                .map(language -> language.equalsIgnoreCase("en"))
                .orElse(false);
    }
}
