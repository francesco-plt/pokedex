package com.pokedex.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokedex.app.domain.Pokemon;
import com.pokedex.app.port.out.LoadPokemonPort;
import org.junit.jupiter.api.Test;

class PokemonServiceTest {

    private final LoadPokemonPort loadPokemonPort = mock(LoadPokemonPort.class);
    private final TranslationService translationService = mock(TranslationService.class);
    private final PokemonService pokemonService = new PokemonService(loadPokemonPort, translationService);

    @Test
    void shouldFetchPokemonAndReplaceOnlyDescriptionWhenTranslated() {
        // Arrange
        Pokemon originalPokemon = Pokemon.builder()
                .name("bulbasaur")
                .description("A strange seed was planted on its back.")
                .habitat("forest")
                .isLegendary(false)
                .build();
        when(loadPokemonPort.getPokemonByName("bulbasaur")).thenReturn(originalPokemon);
        when(translationService.fetchTranslatedPokemonDescription(originalPokemon))
                .thenReturn("A strange seed wast planted on its back.");

        // Act
        Pokemon translatedPokemon = pokemonService.fetchTranslatedByName("bulbasaur");

        // Assert
        assertThat(translatedPokemon).isNotSameAs(originalPokemon);
        assertThat(translatedPokemon.name()).isEqualTo("bulbasaur");
        assertThat(translatedPokemon.description()).isEqualTo("A strange seed wast planted on its back.");
        assertThat(translatedPokemon.habitat()).isEqualTo("forest");
        assertThat(translatedPokemon.isLegendary()).isFalse();
        verify(loadPokemonPort).getPokemonByName("bulbasaur");
        verify(translationService).fetchTranslatedPokemonDescription(originalPokemon);
    }
}
