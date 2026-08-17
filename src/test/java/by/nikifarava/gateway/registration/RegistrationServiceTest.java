package by.nikifarava.gateway.registration;
import by.nikifarava.gateway.registration.client.AuthServiceClient;
import by.nikifarava.gateway.registration.client.UserServiceClient;
import by.nikifarava.gateway.registration.dto.request.CredentialsRequest;
import by.nikifarava.gateway.registration.dto.request.RegisterRequest;
import by.nikifarava.gateway.registration.dto.request.UserProfileRequest;
import by.nikifarava.gateway.registration.dto.response.JwtResponse;
import by.nikifarava.gateway.registration.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.time.Month;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private RegistrationService registrationService;

    private RegisterRequest request;

    private UserResponse user;

    private JwtResponse jwt;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest(
                "Anna", "Ivanova", "anna@mail.com",
                LocalDate.of(2010, Month.MARCH, 1), "anna", "password1");
        user = new UserResponse(1L, "Anna", "Ivanova", "anna@mail.com");
        jwt = new JwtResponse("access", "refresh");
    }

    @Test
    @DisplayName("register - success")
    void registerWhenBothServicesSucceedShouldReturnJwt() {
        when(userServiceClient.createUser(any(UserProfileRequest.class))).thenReturn(Mono.just(user));
        when(authServiceClient.register(any(CredentialsRequest.class))).thenReturn(Mono.just(jwt));

        StepVerifier.create(registrationService.registerUser(request))
                .expectNext(jwt).verifyComplete();

        verify(userServiceClient).createUser(any(UserProfileRequest.class));
        verify(authServiceClient).register(any(CredentialsRequest.class));
        verify(userServiceClient, never()).deleteUser(any());
    }

    @Test
    @DisplayName("register - auth failed, rollback user")
    void registerWhenAuthFailsShouldDeleteUserAndPassError() {
        RuntimeException authError = new RuntimeException("login exists");

        when(userServiceClient.createUser(any(UserProfileRequest.class))).thenReturn(Mono.just(user));
        when(authServiceClient.register(any(CredentialsRequest.class))).thenReturn(Mono.error(authError));
        when(userServiceClient.deleteUser(1L)).thenReturn(Mono.empty());

        StepVerifier.create(registrationService.registerUser(request))
                .expectErrorMatches(ex -> ex == authError)
                .verify();

        verify(userServiceClient).deleteUser(1L);
    }

    @Test
    @DisplayName("register - failed")
    void registerWhenUserCreateFailsShouldNotCallAuthOrDelete() {
        when(userServiceClient.createUser(any(UserProfileRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("user failed")));

        StepVerifier.create(registrationService.registerUser(request))
                .expectErrorMessage("user failed")
                .verify();

        verify(authServiceClient, never()).register(any());
        verify(userServiceClient, never()).deleteUser(any());
    }

    @Test
    @DisplayName("register - rollback failed, returns auth error")
    void registerWhenRollbackFailsShouldPassAuthError() {
        RuntimeException authError = new RuntimeException("auth failed");

        when(userServiceClient.createUser(any(UserProfileRequest.class))).thenReturn(Mono.just(user));
        when(authServiceClient.register(any(CredentialsRequest.class))).thenReturn(Mono.error(authError));
        when(userServiceClient.deleteUser(1L))
                .thenReturn(Mono.error(new RuntimeException("delete failed")));

        StepVerifier.create(registrationService.registerUser(request))
                .expectErrorMatches(ex -> ex == authError)
                .verify();

        verify(userServiceClient).deleteUser(1L);
    }
}
