package by.nikifarava.gateway.security;

import by.nikifarava.gateway.client.AuthServiceClient;
import by.nikifarava.gateway.config.GatewayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final int BEGIN_INDEX = 7;

    private final GatewayProperties gatewayProperties;
    private final AuthServiceClient authServiceClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (gatewayProperties.getSecurity().getPublicPaths().contains(path)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = header.substring(BEGIN_INDEX);

        return authServiceClient.validate(token)
                .then(Mono.defer(() -> chain.filter(exchange)))
                .onErrorResume(ex -> unauthorized(exchange));
    }

    private  Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
