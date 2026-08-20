package by.nikifarava.gateway.security;


import by.nikifarava.gateway.client.AuthServiceClient;
import by.nikifarava.gateway.config.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String JWT_TOKEN = "test-token";

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter jwtFilter;

    @BeforeEach
    void setUp() {
        GatewayProperties gatewayProperties = new GatewayProperties();
        gatewayProperties.getSecurity().setPublicPaths(List.of("/api/auth/login",
                "/api/auth/register",
                "/api/auth/refresh",
                "/api/auth/validate",
                "/actuator/health",
                "/actuator/info"));

        jwtFilter = new JwtAuthenticationFilter(gatewayProperties, authServiceClient);
    }

    @Test
    @DisplayName("filter - public path skips JWT")
    void filterWhenPublicPathShouldSkipJwt() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login"));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());


        StepVerifier.create(jwtFilter.filter(exchange, filterChain)).verifyComplete();

        verify(authServiceClient, never()).validate(anyString());
        verify(filterChain).filter(exchange);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("filter - unauthorized")
    void filterWhenNoAuthorizationShouldReturn401() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders"));

        StepVerifier.create(jwtFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());

        verify(authServiceClient, never()).validate(anyString());
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("filter - header without Bearer")
    void filterWhenNotBearerShouldReturn401() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header("Authorization", "token-token")
                        .build());

        StepVerifier.create(jwtFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());

        verify(authServiceClient, never()).validate(anyString());
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("filter - invalid token")
    void filterWhenTokenInvalidShouldReturn401() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header("Authorization", "Bearer bad-token")
                        .build());

        when(authServiceClient.validate("bad-token")).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(jwtFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());

        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("filter - valid token continues chain")
    void filterWhenTokenValidShouldContinue() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header("Authorization", "Bearer " + JWT_TOKEN)
                        .build());

        when(authServiceClient.validate(JWT_TOKEN)).thenReturn(Mono.empty());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtFilter.filter(exchange, filterChain)).verifyComplete();

        verify(authServiceClient).validate(JWT_TOKEN);
        verify(filterChain).filter(exchange);

        assertNull(exchange.getResponse().getStatusCode());
    }
}
