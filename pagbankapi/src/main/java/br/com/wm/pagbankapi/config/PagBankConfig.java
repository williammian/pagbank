package br.com.wm.pagbankapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PagBankConfig {

    @Value("${pagbank.api.url}")
    private String apiUrl;

    @Value("${pagbank.api.token}")
    private String apiToken;

    @Bean
    public WebClient pagBankWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();
    }
}
