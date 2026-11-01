package com.example.goldapple.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI goldAppleOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Золотое яблоко API")
                        .version("v1")
                        .description("HTTP API учебного интернет-магазина косметики. Суммы указаны в рублях."))
                .addServersItem(new Server().url("http://localhost:8080"));
    }
}
