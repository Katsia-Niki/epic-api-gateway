package by.nikifarava.gateway.registration.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest (@NotBlank String name,
                               @NotBlank String surname,
                               @NotBlank @Email String email,
                               @NotNull @Past LocalDate birthDate,
                               @NotBlank String login,
                               @NotBlank @Size(min = 8) String password){
}

