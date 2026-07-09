package com.bookstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI bookStoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookStore API Documentation")
                        .description("REST API for BookStore Management System")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("BookStore Team")
                                .email("support@bookstore.com")
                                .url("https://bookstore.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Staging Server")))
                .tags(List.of(
                        new Tag().name("Book Management").description("Operations about books"),
                        new Tag().name("Author Management").description("Operations about authors"),
                        new Tag().name("Category Management").description("Operations about categories")
                ));
    }
}