package com.indivaragroup.jatistore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("https://www.jatistore.app").description("Frontend Vercel Proxy"),
                        new Server().url("https://jatistore-production.up.railway.app").description("Direct Railway Production"),
                        new Server().url("http://localhost:8080").description("Local Development")
                ))
                .info(new Info()
                        .title("JatiStore REST API")
                        .version("1.0")
                        .description("REST API Documentation for JatiStore E-Commerce Platform"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
