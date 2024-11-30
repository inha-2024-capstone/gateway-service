package com.yoger.springgateway.globalfilter;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class HeaderIPExtractor {
    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "HTTP_FORWARDED",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-RealIP",
            "X-Real-IP",
            "REMOTE_ADDR"
    );

    public static String getIPFrom(ServerWebExchange exchange) {
        HttpHeaders headers = getHeaders(exchange);
        // IP_HEADERS 목록에서 클라이언트 IP 확인
        for(String header : IP_HEADERS){
            String firstHeaderValue = headers.getFirst(header);
            if(firstHeaderValue != null && !firstHeaderValue.isEmpty()){
                String[] ips = firstHeaderValue.split(",");
                for(String ip : ips){
                    String trimmedIp = ip.trim();
                    if(isValidateIp(trimmedIp)){
                        return trimmedIp;
                    }
                }
            }
        }
        // 모든 헤더에서 IP를 찾지 못한 경우, 기본적으로 제공되는 원격 주소 반환
        ServerHttpRequest request = exchange.getRequest();
        if(request.getRemoteAddress() != null){
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private static boolean isValidateIp(String trimmedIp) {
        String ipRegex = "\\b((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\b";
        return trimmedIp.matches(ipRegex);
    }

    private static HttpHeaders getHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return request.getHeaders();
    }


}
