package pl.pw.edu.po.search_engine.simplesearchengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Simple Search Engine API")
                        .version("1.0.0")
                        .description("RESTful API for document management and full-text search engine. " +
                                "Supports CRUD operations, TF-IDF ranking, and future web crawling capabilities.")
                        .contact(new Contact()
                                .name("Search Engine Team")
                                .url("https://github.com/yourusername/simple-search-engine")));
    }
}

