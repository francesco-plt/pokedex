package com.pokedex.app.adapter.in.web;

import com.pokedex.app.adapter.in.web.model.PokemonResponse;
import com.pokedex.app.service.PokemonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    @GetMapping("/{name}")
    public PokemonResponse getPokemon(@PathVariable String name) {
        return Optional.of(name)
                .map(pokemonService::fetchByName)
                .map(PokemonResponseMapper::toPokemonResponse)
                .orElseThrow();
    }

    @GetMapping("/translated/{name}")
    public String getTranslatedPokemonDescription(@PathVariable String name) {
        return "hello_world";
    }
}
