package by.nikifarava.gateway.registration;

import by.nikifarava.gateway.registration.dto.request.RegisterRequest;
import by.nikifarava.gateway.registration.dto.response.JwtResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    @Test
    @DisplayName("register - success")
    void registerShouldReturnCreated() {
        RegisterRequest request = new RegisterRequest(
                "Anna", "Niki", "anna@mail.com",
                LocalDate.of(2000, Month.APRIL, 1), "anna", "password");

        JwtResponse jwt = new JwtResponse("access", "refresh");

        when(registrationService.registerUser(request)).thenReturn(Mono.just(jwt));

        StepVerifier.create(registrationController.register(request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatusCode());
                    assertEquals(jwt, response.getBody());
                })
                .verifyComplete();
    }
}
