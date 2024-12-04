package com.yoger.springgateway.globalfilter;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            int statusCode = 0;
            if (response.getStatusCode() != null) {
                statusCode = response.getStatusCode().value();
            }
            log.info(
                    "Timestamp={}\nRequestID={}\nDuration={}\nMethod={}\nURI={}\nStatusCode={}\nClient IP={}\nHeaders={}\n",
                    LocalDateTime.now(), request.getId(), System.currentTimeMillis() - start, request.getMethod(),
                    request.getURI(), statusCode, HeaderIPExtractor.getIPFrom(exchange), request.getHeaders());
        }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
