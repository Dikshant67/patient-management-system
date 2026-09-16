package com.pm.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JwtValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.url}") String authServiceUrl) {

        this.webClient = webClientBuilder
                .baseUrl(authServiceUrl)
                .build();
    }

    @Override
    public GatewayFilter apply(Object config) {

        return (exchange, chain) -> {

            String token = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // No JWT
            if (token == null || !token.startsWith("Bearer ")) {

                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();
            }

            // Ask Auth Service to validate JWT
            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .exchangeToMono(response -> {

                        // JWT is valid
                        if (response.statusCode().is2xxSuccessful()) {
                            return chain.filter(exchange);
                        }

                        // JWT is invalid
                        exchange.getResponse()
                                .setStatusCode(response.statusCode());

                        return exchange.getResponse().setComplete();
                    });
        };
    }
}