package by.nikifarava.gateway.registration.dto.request;

import java.time.LocalDate;

public record UserProfileRequest(String name, String surname, String email, LocalDate birthDate) {
}
