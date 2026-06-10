package com.pokedex.app.adapter.out.http.pokeapi;

import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiNotFoundException;
import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiUnavailableException;
import com.pokedex.app.domain.Pokemon;
import com.pokedex.app.domain.exception.PokemonFetchException;
import com.pokedex.app.domain.exception.PokemonNotFoundException;
import com.pokedex.app.domain.exception.PokemonServiceUnavailableException;
import com.pokedex.app.port.out.LoadPokemonPort;
import feign.RetryableException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokeApiAdapter implements LoadPokemonPort {

    private final PokeApiClient pokeApiClient;

    @Override
    @NotNull
    public Pokemon getPokemonByName(@NotNull String name) {
        log.debug("Fetching pokemon '{}' from PokeAPI.", name);
        try {
            return Optional.of(name)
                    .map(pokeApiClient::getPokemonSpecieByName)
                    .map(PokeApiPokemonMapper::fromSpecieToPokemon)
                    .orElseThrow();

        } catch (PokeApiNotFoundException ex) {
            throw new PokemonNotFoundException(ex.pokemonName());
        } catch (PokeApiUnavailableException | RetryableException ex) {
            throw new PokemonServiceUnavailableException(
                "PokeAPI unavailable while fetching pokemon '%s'.".formatted(name), ex);
        } catch (RuntimeException ex) {
            throw new PokemonFetchException(
                "Unexpected error while fetching pokemon '%s' from PokeAPI.".formatted(name), ex);
        }
    }
}
