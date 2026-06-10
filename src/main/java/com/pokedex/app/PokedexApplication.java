package com.pokedex.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PokedexApplication {

    static void main(String[] args) {
        SpringApplication.run(PokedexApplication.class, args);
    }
}
