package com.yoger.springgateway.filter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;


@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient webClient;
    private static final String USER_HEADER_NAME = "User-Id";
    private static final String COMPANY_HEADER_NAME = "Company-Id";

    public AuthFilter(WebClient webClient) {
        super(Config.class);
        this.webClient = webClient;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            Optional<String> accessToken = extractAccessToken(exchange.getRequest().getHeaders());

            if (accessToken.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return webClient.get().uri(config.getAuthServerUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.get()).exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            String userId = response.headers().asHttpHeaders().getFirst(USER_HEADER_NAME);
                            if (userId != null && !userId.isEmpty()) {
                                ServerHttpRequest builtRequest = exchange.getRequest().mutate()
                                        .header(USER_HEADER_NAME, userId).build();
                                ServerWebExchange buildExchange = exchange.mutate().request(builtRequest).build();
                                log.info("User-Id={} is sent", buildExchange.getRequest().getHeaders().getFirst(USER_HEADER_NAME));
                                return chain.filter(buildExchange);
                            }
                        }
                        log.info("status=UNAUTHORIZED");
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }).onErrorResume(e -> {
                        log.info("status=INTERNAL-SERVER-ERROR");
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private Optional<String> extractAccessToken(HttpHeaders headers) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }

    @Getter
    @Setter
    public static class Config {
        private String authServerUrl;
    }
}
