package by.nikifarava.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient authWebClient(GatewayProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getServices().getAuthUri())
                .build();
    }
}
