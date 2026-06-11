package com.pokedex.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokedex.app.domain.Pokemon;
import com.pokedex.app.port.out.TranslatePokemonDescriptionPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TranslationServiceTest {

    private final TranslatePokemonDescriptionPort translatePokemonDescriptionPort =
            mock(TranslatePokemonDescriptionPort.class);
    private final TranslationService translationService =
            new TranslationService(translatePokemonDescriptionPort);

    @Test
    void shouldTranslateToYodaForLegendaryPokemon() {
        // Arrange
        Pokemon pokemon = Pokemon.builder()
                .name("mewtwo")
                .description("A genetically engineered pokemon.")
                .habitat("rare")
                .isLegendary(true)
                .build();
        when(translatePokemonDescriptionPort.translateToYoda("A genetically engineered pokemon."))
                .thenReturn(Optional.of("Engineered genetically, a pokemon it is."));

        // Act
        String result = translationService.fetchTranslatedPokemonDescription(pokemon);

        // Assert
        assertThat(result).isEqualTo("Engineered genetically, a pokemon it is.");
        verify(translatePokemonDescriptionPort).translateToYoda("A genetically engineered pokemon.");
        verify(translatePokemonDescriptionPort, never()).translateToShakespeare(anyString());
    }

    @Test
    void shouldTranslateToYodaForCaveHabitatPokemon() {
        // Arrange
        Pokemon pokemon = Pokemon.builder()
                .name("zubat")
                .description("Forms colonies in perpetually dark places.")
                .habitat("cave")
                .isLegendary(false)
                .build();
        when(translatePokemonDescriptionPort.translateToYoda("Forms colonies in perpetually dark places."))
                .thenReturn(Optional.of("Colonies in dark places, forms it does."));

        // Act
        String result = translationService.fetchTranslatedPokemonDescription(pokemon);

        // Assert
        assertThat(result).isEqualTo("Colonies in dark places, forms it does.");
        verify(translatePokemonDescriptionPort).translateToYoda("Forms colonies in perpetually dark places.");
        verify(translatePokemonDescriptionPort, never()).translateToShakespeare(anyString());
    }

    @Test
    void shouldTranslateToShakespeareForNonLegendaryNonCavePokemon() {
        // Arrange
        Pokemon pokemon = Pokemon.builder()
                .name("bulbasaur")
                .description("A strange seed was planted on its back.")
                .habitat("forest")
                .isLegendary(false)
                .build();
        when(translatePokemonDescriptionPort.translateToShakespeare("A strange seed was planted on its back."))
                .thenReturn(Optional.of("A strange seed wast planted on its back."));

        // Act
        String result = translationService.fetchTranslatedPokemonDescription(pokemon);

        // Assert
        assertThat(result).isEqualTo("A strange seed wast planted on its back.");
        verify(translatePokemonDescriptionPort).translateToShakespeare("A strange seed was planted on its back.");
        verify(translatePokemonDescriptionPort, never()).translateToYoda(anyString());
    }

    @Test
    void shouldReturnOriginalDescriptionWhenTranslationReturnsEmpty() {
        // Arrange
        Pokemon pokemon = Pokemon.builder()
                .name("pikachu")
                .description("Electric type pokemon.")
                .habitat("forest")
                .isLegendary(false)
                .build();
        when(translatePokemonDescriptionPort.translateToShakespeare("Electric type pokemon."))
                .thenReturn(Optional.empty());

        // Act
        String result = translationService.fetchTranslatedPokemonDescription(pokemon);

        // Assert
        assertThat(result).isEqualTo("Electric type pokemon.");
        verify(translatePokemonDescriptionPort).translateToShakespeare("Electric type pokemon.");
    }

    @Test
    void shouldReturnEmptyStringWhenDescriptionIsNull() {
        // Arrange
        Pokemon pokemon = Pokemon.builder()
                .name("snorlax")
                .description(null)
                .habitat("forest")
                .isLegendary(false)
                .build();

        // Act
        String result = translationService.fetchTranslatedPokemonDescription(pokemon);

        // Assert
        assertThat(result).isEmpty();
        verify(translatePokemonDescriptionPort, never()).translateToYoda(anyString());
        verify(translatePokemonDescriptionPort, never()).translateToShakespeare(anyString());
    }

    @Test
    void shouldReturnEmptyStringWhenDescriptionIsBlank() {
        // Arrange
        Pokemon pokemon = Pokemon.builder()
                .name("snorlax")
                .description("")
                .habitat("forest")
                .isLegendary(false)
                .build();

        // Act
        String result = translationService.fetchTranslatedPokemonDescription(pokemon);

        // Assert
        assertThat(result).isEmpty();
        verify(translatePokemonDescriptionPort, never()).translateToYoda(anyString());
        verify(translatePokemonDescriptionPort, never()).translateToShakespeare(anyString());
    }
}
