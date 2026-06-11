package com.pokedex.app.service;

import com.pokedex.app.domain.Pokemon;
import com.pokedex.app.port.out.LoadPokemonPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokemonService {

    private final LoadPokemonPort loadPokemonPort;

    private final TranslationService translationService;

    public Pokemon fetchTranslatedByName(String name) {
        return Optional.of(name)
                .map(this::fetchByName)
                .map(pokemon -> pokemon.toBuilder()
                        .description(translationService.fetchTranslatedPokemonDescription(pokemon))
                        .build())
                .orElseThrow();
    }

    public Pokemon fetchByName(String name) {
        return loadPokemonPort.getPokemonByName(name);
    }
}
