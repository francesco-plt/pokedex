package com.pokedex.app.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PokemonDescriptionTranslationLanguage {
    YODA("yoda"),
    SHAKESPEARE("shakespeare");

    private final String language;
}
