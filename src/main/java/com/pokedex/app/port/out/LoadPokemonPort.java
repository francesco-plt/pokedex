package com.pokedex.app.port.out;

import com.pokedex.app.domain.Pokemon;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface LoadPokemonPort {

    @NotNull
    Pokemon getPokemonByName(@NotNull String name);
}
