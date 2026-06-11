package com.pokedex.app.adapter.out.http.funtranslations;

import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationContent;
import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationResponse;

import java.util.Optional;

final class FunTranslationDescriptionMapper {

    static String fromTranslationResponse(FunTranslationsTranslationResponse response) {
        return Optional.ofNullable(response)
                .map(FunTranslationsTranslationResponse::contents)
                .map(FunTranslationsTranslationContent::translated)
                .orElse("");
    }
}
