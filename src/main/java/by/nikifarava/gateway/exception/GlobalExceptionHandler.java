package by.nikifarava.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String TIMEZONE = "Europe/Minsk";


    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String path = exchange.getRequest().getURI().getPath();
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(ZoneId.of(TIMEZONE)),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                path,
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(
            WebClientResponseException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Downstream error: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());

        String path = exchange.getRequest().getURI().getPath();
        int status = ex.getStatusCode().value();
        String message = ex.getStatusText();

        HttpStatus httpStatus = HttpStatus.resolve(status);

        if (httpStatus == null) {
            httpStatus = HttpStatus.BAD_GATEWAY;
            status = httpStatus.value();
            message = "Downstream service error";
        }

        if (status >= 500) {
            httpStatus = HttpStatus.BAD_GATEWAY;
            status = httpStatus.value();
            message = "Downstream service error";
        }

        ErrorResponse response = buildBody(status, message, path, null);
        return ResponseEntity.status(httpStatus).body(response);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequest(
            WebClientRequestException ex,
            ServerWebExchange exchange
    ) {
        log.error("Cannot reach downstream service", ex);

        ErrorResponse response = buildBody(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service unavailable",
                exchange.getRequest().getURI().getPath(),
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            ServerWebExchange exchange
    ) {
        log.error("Unexpected exception", ex);

        ErrorResponse response = buildBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                exchange.getRequest().getURI().getPath(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ErrorResponse buildBody(int status, String message, String path, Map<String, String> errors) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneId.of(TIMEZONE)),
                status,
                message,
                path,
                errors
        );
    }
}
