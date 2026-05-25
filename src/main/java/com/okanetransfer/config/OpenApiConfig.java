package com.okanetransfer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/okane_transfer_war").description("Okane Transfer Server"))
                .info(new Info()
                        .title("Okane Transfer API")
                        .version("1.0")
                        .description("API de transfert d'argent"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("okane-transfer")
                .pathsToMatch("/api/**")
                .build();
    }
}