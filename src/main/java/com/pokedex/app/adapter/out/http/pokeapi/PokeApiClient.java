package com.pokedex.app.adapter.out.http.pokeapi;

import com.pokedex.app.adapter.out.http.pokeapi.model.PokeApiPokemonSpecieResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "poke-api-client",
    configuration = PokeApiClientConfig.class
)
public interface PokeApiClient {

    @GetMapping("/pokemon-species/{name}")
    PokeApiPokemonSpecieResponse getPokemonSpecieByName(@PathVariable String name);
}
