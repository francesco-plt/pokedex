package com.pokedex.app.adapter.out.http.pokeapi;

import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiUnexpectedResponseException;
import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiUnavailableException;
import com.pokedex.app.adapter.out.http.pokeapi.exception.PokeApiNotFoundException;
import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.net.URI;
import java.util.Optional;

final class PokeApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String pokemonName = extractPokemonNameFromRequestPath(response);

        if (status == 404) {
            return new PokeApiNotFoundException(
                pokemonName,
                "Pokemon '%s' was not found at PokeAPI.".formatted(pokemonName)
            );
        }

        if (status == 429 || status >= 500) {
            return new PokeApiUnavailableException(
                "PokeAPI returned HTTP %d while fetching pokemon '%s'.".formatted(status, pokemonName)
            );
        }

        return new PokeApiUnexpectedResponseException(
            "PokeAPI returned unexpected HTTP %d while fetching pokemon '%s'.".formatted(status, pokemonName)
        );
    }

    private static String extractPokemonNameFromRequestPath(Response response) {
        return Optional.ofNullable(response.request())
            .map(Request::url)
            .map(URI::create)
            .map(URI::getPath)
            .map(path -> {
                // assuming no trailing slash in the request url
                int index = path.lastIndexOf('/');
                return index >= 0 ? path.substring(index + 1) : path;
            })
            .filter(value -> !value.isBlank())
            .orElse("unknown");
    }
}
