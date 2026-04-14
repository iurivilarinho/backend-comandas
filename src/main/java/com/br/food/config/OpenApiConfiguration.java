package com.br.food.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI barTabsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Bar Tabs API")
                .description("API for managing tabs, tables, stock, and sales in bars and restaurants.")
                .version("1"));
    }
}
