package com.pokedex.app.adapter.out.http.funtranslations;

import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationRequest;
import com.pokedex.app.adapter.out.http.funtranslations.model.FunTranslationsTranslationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "funtranslations-client",
    configuration = FunTranslationsClientConfig.class
)
public interface FunTranslationsClient {

    @PostMapping("/translate/yoda")
    FunTranslationsTranslationResponse translateYoda(@RequestBody FunTranslationsTranslationRequest request);

    @PostMapping("/translate/shakespeare")
    FunTranslationsTranslationResponse translateShakespeare(@RequestBody FunTranslationsTranslationRequest request);
}
