package com.pokedex.app.domain;

import lombok.Builder;

@Builder(toBuilder = true)
public record Pokemon(
        String name,
        String description,
        String habitat,
        boolean isLegendary) {}
