package by.nikifarava.gateway.registration;

import by.nikifarava.gateway.registration.client.AuthServiceClient;
import by.nikifarava.gateway.registration.client.UserServiceClient;
import by.nikifarava.gateway.registration.dto.request.CredentialsRequest;
import by.nikifarava.gateway.registration.dto.request.RegisterRequest;
import by.nikifarava.gateway.registration.dto.request.UserProfileRequest;
import by.nikifarava.gateway.registration.dto.response.JwtResponse;
import by.nikifarava.gateway.registration.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserServiceClient userServiceClient;
    private final AuthServiceClient authServiceClient;

    public Mono<JwtResponse> registerUser(RegisterRequest registerRequest) {
        UserProfileRequest profile = new UserProfileRequest(registerRequest.name(),
                registerRequest.surname(), registerRequest.email(), registerRequest.birthDate());

        return userServiceClient.createUser(profile)
                .flatMap(user -> createCredentials(user, registerRequest)
                        .onErrorResume(ex -> rollbackUser(user.id()).then(Mono.error(ex)))
                );
    }

    private Mono<JwtResponse> createCredentials(UserResponse user, RegisterRequest request) {
        CredentialsRequest credentials = new CredentialsRequest(
                user.id(), request.login(), request.password());
        return authServiceClient.register(credentials);
    }

    private Mono<Void> rollbackUser(Long userId) {
        return userServiceClient.deleteUser(userId)
                .doOnError(rollbackEx -> log.error("Failed to delete user {} while rollback", userId, rollbackEx))
                .onErrorResume(rollbackEx -> Mono.empty());
    }
}
