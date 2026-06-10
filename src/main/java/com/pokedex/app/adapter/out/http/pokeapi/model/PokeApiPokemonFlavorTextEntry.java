package com.pokedex.app.adapter.out.http.pokeapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiPokemonFlavorTextEntry(
        @JsonProperty("flavor_text")
        String flavorText) {

    public PokeApiPokemonFlavorTextEntry {
        if (flavorText != null) {
            flavorText = flavorText
                    .replace('\f', '\n')    // PokeAPI page-break marker
                    .replace("\r\n", "\n")
                    .replace('\r', '\n')
                    .trim();
        }
    }
}
