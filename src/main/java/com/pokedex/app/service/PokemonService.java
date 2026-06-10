package com.pokedex.app.service;

import com.pokedex.app.domain.Pokemon;
import com.pokedex.app.port.out.LoadPokemonPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokemonService {

    private final LoadPokemonPort loadPokemonPort;

    public Pokemon fetchByName(String name) {
        return loadPokemonPort.getPokemonByName(name);
    }
}
