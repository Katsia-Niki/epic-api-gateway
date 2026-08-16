package by.nikifarava.gateway.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class GatewayProperties {
    @Valid
    @NotNull
    private Jwt jwt = new Jwt();
    @Valid
    @NotNull
    private Security security = new Security();
    @Valid
    @NotNull
    private Services services = new Services();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        private String secret;
        @NotBlank
        private String issuer;
    }

    @Getter
    @Setter
    public static class Security {
        @NotEmpty
        private List<String> publicPaths = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Services {
        @NotBlank
        private String authUri;
        @NotBlank
        private String userUri;
    }
}
