package com.pokedex.app.adapter.out.http.funtranslations;

import com.pokedex.app.adapter.out.http.funtranslations.exception.FunTranslationsUnavailableException;
import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationRequest;
import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationResponse;
import com.pokedex.app.port.out.TranslatePokemonDescriptionPort;
import feign.RetryableException;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FunTranslationsAdapter implements TranslatePokemonDescriptionPort {

    private final FunTranslationsClient funTranslationsClient;

    @Override
    @NotNull
    public Optional<String> translateToYoda(@NotNull String description) {
        return translate(
            description,
            funTranslationsClient::translateYoda
        );
    }

    @Override
    @NotNull
    public Optional<String> translateToShakespeare(@NotNull String description) {
        return translate(
            description,
            funTranslationsClient::translateShakespeare
        );
    }

    private static FunTranslationsTranslationRequest requestWithText(String text) {
        return FunTranslationsTranslationRequest.builder()
            .text(text)
            .build();
    }

    private Optional<String> translate(
        String description,
        Function<FunTranslationsTranslationRequest, FunTranslationsTranslationResponse> translator
    ) {
        try {
            return Optional.of(description)
                .map(FunTranslationsAdapter::requestWithText)
                .map(translator)
                .map(FunTranslationDescriptionMapper::fromTranslationResponse);
        } catch (FunTranslationsUnavailableException | RetryableException ex) {
            log.warn("Translation unavailable: {}", ex.getMessage());
            return Optional.empty();
        } catch (RuntimeException ex) {
            log.error("Unexpected error from FunTranslations", ex);
            return Optional.empty();
        }
    }
}
