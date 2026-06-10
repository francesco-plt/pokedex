package com.pokedex.app.adapter.out.http.pokeapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiPokemonSpecieResponse(
        String name,
        PokeApiPokemonHabitat habitat,
        @JsonProperty("is_legendary")
        boolean isLegendary,
        @JsonProperty("flavor_text_entries")
        List<PokeApiPokemonFlavorTextEntry> flavorTextEntries) {}
