package by.nikifarava.gateway.registration.client;

import by.nikifarava.gateway.registration.dto.request.UserProfileRequest;
import by.nikifarava.gateway.registration.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient userWebClient;

    public UserServiceClient(@Qualifier("userWebClient") WebClient userWebClient) {
        this.userWebClient = userWebClient;
    }

    public Mono<UserResponse> createUser(UserProfileRequest userProfileRequest) {
        return userWebClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userProfileRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    public Mono<Void> deleteUser(Long userId) {
        return userWebClient.delete()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
