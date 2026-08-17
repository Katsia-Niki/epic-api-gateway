package by.nikifarava.gateway.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/register").build());
    }

    @Test
    @DisplayName("validation - 400")
    void validationShouldReturnBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("request", "password",
                "password must be at least 8 symbols")));

        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().message());
        assertEquals("password must be at least 8 symbols", response.getBody().errors().get("password"));
        assertEquals("/api/auth/register", response.getBody().path());
    }

    @Test
    @DisplayName("WebClient 409 - keep conflict")
    void handleWebClientResponseWhenConflictShouldReturnConflict() {
        WebClientResponseException ex = WebClientResponseException.create(
                409, "Conflict", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        ResponseEntity<ErrorResponse> response = handler.handleWebClientResponse(ex, exchange);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("WebClient 500 - map to BAD_GATEWAY")
    void handleWebClientResponseWhenServerErrorShouldReturn502() {
        WebClientResponseException ex = WebClientResponseException.create(
                500, "Internal Server Error", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        ResponseEntity<ErrorResponse> response = handler.handleWebClientResponse(ex, exchange);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Downstream service error", response.getBody().message());
    }

    @Test
    @DisplayName("WebClient request - SERVICE_UNAVAILABLE")
    void handleWebClientRequestShouldReturnServiceUnavailable() {
        WebClientRequestException ex = new WebClientRequestException(
                new RuntimeException("connection refused"),
                HttpMethod.POST,
                URI.create("http://localhost:8083/api/auth/register"),
                new HttpHeaders());

        ResponseEntity<ErrorResponse> response = handler.handleWebClientRequest(ex, exchange);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service unavailable", response.getBody().message());
    }

}
