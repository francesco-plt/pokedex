package com.pokedex.app.adapter.out.http.funtranslations.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FunTranslationsTranslationResponse(FunTranslationsTranslationContent contents) {}
