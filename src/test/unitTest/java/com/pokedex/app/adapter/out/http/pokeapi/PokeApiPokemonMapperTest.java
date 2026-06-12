package com.pokedex.app.adapter.out.http.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonFlavorTextEntry;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonHabitat;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonLanguage;
import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonSpecieResponse;
import com.pokedex.app.domain.Pokemon;
import java.util.List;
import org.junit.jupiter.api.Test;

class PokeApiPokemonMapperTest {

    @Test
    void shouldMapSpeciesResponseToDomainPokemon() {
        // Arrange
        PokeApiPokemonSpecieResponse species = PokeApiPokemonSpecieResponse.builder()
            .name("pikachu")
            .habitat(PokeApiPokemonHabitat.builder()
                    .name("forest")
                    .build())
            .isLegendary(false)
            .flavorTextEntries(List.of(
                PokeApiPokemonFlavorTextEntry.builder()
                        .flavorText(" electric\fmouse\r\npokemon ")
                        .language(PokeApiPokemonLanguage.builder().name("en").build())
                        .build(),
                PokeApiPokemonFlavorTextEntry.builder()
                        .flavorText("ignored")
                        .language(PokeApiPokemonLanguage.builder().name("en").build())
                        .build()
            ))
            .build();

        // Act
        Pokemon pokemon = PokeApiPokemonMapper.fromSpecieToPokemon(species);

        // Assert
        assertThat(pokemon.name()).isEqualTo("pikachu");
        assertThat(pokemon.description()).isEqualTo("electric\nmouse\npokemon");
        assertThat(pokemon.habitat()).isEqualTo("forest");
        assertThat(pokemon.isLegendary()).isFalse();
    }

    @Test
    void shouldFallbackToEmptyDescriptionAndHabitatWhenMissing() {
        // Arrange
        PokeApiPokemonSpecieResponse species = PokeApiPokemonSpecieResponse.builder()
            .name("ditto")
            .isLegendary(false)
            .build();

        // Act
        Pokemon pokemon = PokeApiPokemonMapper.fromSpecieToPokemon(species);

        // Assert
        assertThat(pokemon.name()).isEqualTo("ditto");
        assertThat(pokemon.description()).isEmpty();
        assertThat(pokemon.habitat()).isEmpty();
        assertThat(pokemon.isLegendary()).isFalse();
    }

    @Test
    void shouldFallbackToEmptyDescriptionWhenFlavorTextListIsEmpty() {
        // Arrange
        PokeApiPokemonSpecieResponse species = PokeApiPokemonSpecieResponse.builder()
            .name("psyduck")
            .habitat(PokeApiPokemonHabitat.builder()
                    .name("waters-edge")
                    .build())
            .isLegendary(false)
            .flavorTextEntries(List.of())
            .build();

        // Act
        Pokemon pokemon = PokeApiPokemonMapper.fromSpecieToPokemon(species);

        // Assert
        assertThat(pokemon.name()).isEqualTo("psyduck");
        assertThat(pokemon.description()).isEmpty();
        assertThat(pokemon.habitat()).isEqualTo("waters-edge");
        assertThat(pokemon.isLegendary()).isFalse();
    }

    @Test
    void shouldPreferEnglishDescriptionWhenMultipleLanguagesArePresent() {
        // Arrange
        PokeApiPokemonSpecieResponse species = PokeApiPokemonSpecieResponse.builder()
            .name("pikachu")
            .habitat(PokeApiPokemonHabitat.builder()
                .name("forest")
                .build())
            .isLegendary(false)
            .flavorTextEntries(List.of(
                PokeApiPokemonFlavorTextEntry.builder()
                    .flavorText("Japanese description")
                    .language(PokeApiPokemonLanguage.builder().name("ja").build())
                    .build(),
                PokeApiPokemonFlavorTextEntry.builder()
                    .flavorText("English description")
                    .language(PokeApiPokemonLanguage.builder().name("en").build())
                    .build()
            ))
            .build();

        // Act
        Pokemon pokemon = PokeApiPokemonMapper.fromSpecieToPokemon(species);

        // Assert
        assertThat(pokemon.description()).isEqualTo("English description");
    }

    @Test
    void shouldReturnEmptyDescriptionWhenEnglishEntryIsMissing() {
        // Arrange
        PokeApiPokemonSpecieResponse species = PokeApiPokemonSpecieResponse.builder()
            .name("pikachu")
            .isLegendary(false)
            .flavorTextEntries(List.of(
                PokeApiPokemonFlavorTextEntry.builder()
                    .flavorText("Japanese description")
                    .language(PokeApiPokemonLanguage.builder().name("ja").build())
                    .build()
            ))
            .build();

        // Act
        Pokemon pokemon = PokeApiPokemonMapper.fromSpecieToPokemon(species);

        // Assert
        assertThat(pokemon.description()).isEmpty();
    }
}
