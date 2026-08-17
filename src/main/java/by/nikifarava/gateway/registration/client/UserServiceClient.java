package by.nikifarava.gateway.registration.client;

import by.nikifarava.gateway.config.GatewayProperties;
import by.nikifarava.gateway.registration.dto.request.UserProfileRequest;
import by.nikifarava.gateway.registration.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private static final String INTERNAL_KEY_HEADER = "X-Internal-Key";

    private final WebClient userWebClient;
    private final String internalKey;

    public UserServiceClient(@Qualifier("userWebClient") WebClient userWebClient,
                             GatewayProperties properties) {
        this.userWebClient = userWebClient;
        this.internalKey = properties.getServices().getInternalKey();
    }

    public Mono<UserResponse> createUser(UserProfileRequest userProfileRequest) {
        return userWebClient.post()
                .uri("/api/users")
                .header(INTERNAL_KEY_HEADER, internalKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userProfileRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    public Mono<Void> deleteUser(Long userId) {
        return userWebClient.delete()
                .uri("/api/users/{id}", userId)
                .header(INTERNAL_KEY_HEADER, internalKey)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
