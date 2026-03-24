package com.instabuy.api_gateway.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Allow public APIs
        if (path.startsWith("/auth") || path.startsWith("/payments")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        try {
            String token = authHeader.substring(7);

            // Validate token
            if (!jwtService.isTokenValid(token)) {
                return handleError(exchange, HttpStatus.UNAUTHORIZED, "Token expired or invalid");
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            // ADMIN-only endpoints
            if (path.startsWith("/admin") && !"ADMIN".equals(role)) {
                return handleError(exchange, HttpStatus.FORBIDDEN, "Access denied (ADMIN only)");
            }

            // Inventory POST → ADMIN only
            if (path.startsWith("/inventory")
                    && (
                    exchange.getRequest().getMethod().name().equals("POST") ||
                            exchange.getRequest().getMethod().name().equals("PUT") ||
                            exchange.getRequest().getMethod().name().equals("DELETE")
            )
                    && !role.contains("ADMIN")) {

                return handleError(exchange, HttpStatus.FORBIDDEN, "Only ADMIN can modify inventory");
            }

            // Add headers
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Name", username)
                            .header("X-User-Role", role)
                    ).build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            return handleError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or malformed token");
        }
    }

    // 🔥 NEW METHOD (COMMON ERROR HANDLER)
    private Mono<Void> handleError(ServerWebExchange exchange,
                                   HttpStatus status,
                                   String message) {

        exchange.getResponse().setStatusCode(status);

        String body = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}",
                status.getReasonPhrase(),
                message
        );

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}