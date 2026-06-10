package com.pokedex.app.adapter.out.http.pokeapi;

import feign.Client;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration(proxyBeanMethods = false)
public class PokeApiClientConfig {

    @Bean
    public okhttp3.OkHttpClient pokeApiOkHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(5))
            .callTimeout(Duration.ofSeconds(5))
            .retryOnConnectionFailure(false)
            .build();
    }

    @Bean
    public Client feignClient(okhttp3.OkHttpClient pokeApiOkHttpClient) {
        return new OkHttpClient(pokeApiOkHttpClient);
    }

    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public ErrorDecoder pokeApiErrorDecoder() {
        return new PokeApiErrorDecoder();
    }
}
