package com.pokedex.app.adapter.out.http.funtranslations;

import feign.Client;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;

public class FunTranslationsClientConfig {

    @Bean
    public okhttp3.OkHttpClient funTranslationsOkHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(5))
            .callTimeout(Duration.ofSeconds(5))
            .retryOnConnectionFailure(false)
            .build();
    }

    @Bean
    public Client funTranslationsFeignClient(okhttp3.OkHttpClient funTranslationsOkHttpClient) {
        return new OkHttpClient(funTranslationsOkHttpClient);
    }

    @Bean
    public Retryer funTranslationsFeignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public ErrorDecoder funTranslationsErrorDecoder() {
        return new FunTranslationsErrorDecoder();
    }
}
