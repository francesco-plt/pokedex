package com.pokedex.app.adapter.in.web;

import com.pokedex.app.domain.exception.PokemonFetchException;
import com.pokedex.app.domain.exception.PokemonNotFoundException;
import com.pokedex.app.domain.exception.PokemonServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PokedexExceptionHandler {

    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePokemonNotFound(
        PokemonNotFoundException ex,
        HttpServletRequest request
    ) {
        ProblemDetail problemDetail = buildProblemDetail(
            HttpStatus.NOT_FOUND,
            "Pokemon not found",
            detail(ex.getMessage(), "Pokemon was not found."),
            request.getRequestURI()
        );
        problemDetail.setProperty("pokemon", ex.pokemonName());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail);
    }

    @ExceptionHandler(PokemonServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handlePokeApiUnavailable(
        PokemonServiceUnavailableException ex,
        HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.SERVICE_UNAVAILABLE,
            "PokeAPI unavailable",
            detail(ex.getMessage(), "PokeAPI is temporarily unavailable."),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(PokemonFetchException.class)
    public ResponseEntity<ProblemDetail> handlePokemonFetchError(
        PokemonFetchException ex,
        HttpServletRequest req
    ) {
        return buildResponse(
            HttpStatus.BAD_GATEWAY,
            "PokeAPI error",
            detail(ex.getMessage(), "PokeAPI returned an unexpected error."),
            req.getRequestURI()
        );
    }

    private static ResponseEntity<ProblemDetail> buildResponse(
        HttpStatus status,
        String title,
        String detail,
        String requestUri
    ) {
        ProblemDetail problemDetail = buildProblemDetail(status, title, detail, requestUri);
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail);
    }

    private static ProblemDetail buildProblemDetail(
        HttpStatus status,
        String title,
        String detail,
        String requestUri
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(requestUri));
        return problemDetail;
    }

    private static String detail(@Nullable String message, String fallback) {
        return Objects.requireNonNullElse(message, fallback);
    }
}
