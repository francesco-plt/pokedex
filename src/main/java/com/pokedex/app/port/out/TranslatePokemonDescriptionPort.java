package com.pokedex.app.port.out;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
public interface TranslatePokemonDescriptionPort {

    @NotNull
    Optional<String> translateToYoda(@NotNull String description);

    @NotNull
    Optional<String> translateToShakespeare(@NotNull String description);
}
