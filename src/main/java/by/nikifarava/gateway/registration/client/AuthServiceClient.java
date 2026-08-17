package by.nikifarava.gateway.registration.client;

import by.nikifarava.gateway.registration.dto.request.CredentialsRequest;
import by.nikifarava.gateway.registration.dto.response.JwtResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {

    private final WebClient authWebClient;

    public AuthServiceClient(@Qualifier("authWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    public Mono<JwtResponse> register(CredentialsRequest credentialsRequest) {
        return authWebClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentialsRequest)
                .retrieve()
                .bodyToMono(JwtResponse.class);
    }
}
