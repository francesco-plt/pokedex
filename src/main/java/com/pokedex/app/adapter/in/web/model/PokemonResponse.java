package com.pokedex.app.adapter.in.web.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record PokemonResponse(
    String name,
    String description,
    String habitat,
    boolean isLegendary) {}
