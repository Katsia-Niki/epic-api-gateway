package by.nikifarava.gateway.security;

import by.nikifarava.gateway.config.GatewayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final int BEGIN_INDEX = 7;

    private final GatewayProperties gatewayProperties;
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
       String path = exchange.getRequest().getURI().getPath();

       if(gatewayProperties.getSecurity().getPublicPaths().contains(path)){
           return chain.filter(exchange);
       }

       String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

       if(header==null || !header.startsWith("Bearer ")){
           return unauthorized(exchange);
       }

       String token = header.substring(BEGIN_INDEX);

       if(!jwtService.isValid(token)){
           return unauthorized(exchange);
       }

       var claims = jwtService.parse(token);

       ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
               .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
               .header("X-User-Role", claims.get("role", String.class))
               .build();

       return chain.filter(exchange.mutate().request(mutatedRequest).build());
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
