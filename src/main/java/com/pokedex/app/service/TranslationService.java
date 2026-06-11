package com.pokedex.app.service;

import com.pokedex.app.domain.Pokemon;
import com.pokedex.app.domain.constant.PokemonDescriptionTranslationLanguage;
import com.pokedex.app.port.out.TranslatePokemonDescriptionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslatePokemonDescriptionPort translatePokemonDescriptionPort;

    public String fetchTranslatedPokemonDescription(Pokemon pokemon) {
        log.debug("Translating description for pokemon {} - habitat {} - legendary {}",
                pokemon.name(), pokemon.habitat(), pokemon.isLegendary());

        if (pokemon.description() == null || pokemon.description().isEmpty()) {
            return "";
        }

        return Optional.of(pokemon)
                .map(this::fetchTranslationLanguageForPokemon)
                .flatMap(language -> switch (language) {
                    case YODA -> translatePokemonDescriptionPort.translateToYoda(pokemon.description());
                    case SHAKESPEARE -> translatePokemonDescriptionPort.translateToShakespeare(pokemon.description());
                })
                .orElse(pokemon.description());
    }

    private PokemonDescriptionTranslationLanguage fetchTranslationLanguageForPokemon(Pokemon pokemon) {
       return pokemon.isLegendary() || "cave".equalsIgnoreCase(pokemon.habitat())
               ? PokemonDescriptionTranslationLanguage.YODA
               : PokemonDescriptionTranslationLanguage.SHAKESPEARE;
    }
}
