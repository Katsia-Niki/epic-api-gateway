package by.nikifarava.gateway.registration;

import by.nikifarava.gateway.registration.dto.request.RegisterRequest;
import by.nikifarava.gateway.registration.dto.response.JwtResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public Mono<ResponseEntity<JwtResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.registerUser(request)
                .map(jwtResponse -> ResponseEntity.status(HttpStatus.CREATED).body(jwtResponse));
    }
}
