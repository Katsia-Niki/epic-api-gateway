package by.nikifarava.gateway.client;

import by.nikifarava.gateway.dto.TokenRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient authWebClient;

    public Mono<Void> validate(String token) {
        return authWebClient.post()
                .uri("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TokenRequestDto(token))
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
