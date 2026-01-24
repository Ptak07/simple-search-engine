package pl.pw.edu.po.search_engine.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for REST clients to communicate with backend API.
 */
@Configuration
public class RestClientConfig {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    @Bean
    public WebClient webClient() {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();

        return WebClient.builder()
                .baseUrl(backendApiUrl)
                .defaultHeader("Content-Type", "application/json")
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}
