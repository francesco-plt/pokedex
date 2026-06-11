package com.pokedex.app.adapter.out.http.funtranslations.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record FunTranslationsTranslationRequest(String text) {}
