package com.study.odersystem;

import java.util.List;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter {
    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    private static final List<String> ALLOWED_PATHS = List.of(
            "/member/create",
            "/member/doLogin",
            "/member/refresh-at",
            "/product/list",
            "**/health"
    );

    private static final List<String> ALLOWED_PATHS_ADMIN = List.of(
            "/member/list",
            "/product/create"
    );
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        // token 검증
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String urlPath = exchange.getRequest().getURI().getRawPath();

        // 인증이 필요 없는 경로는 필터 통과
        if (ALLOWED_PATHS.contains(urlPath)) {
            return chain.filter(exchange);
        }
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("토큰이 없거나 올바른 형식이 아닙니다.");
            }

            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if (ALLOWED_PATHS_ADMIN.contains(urlPath) && !role.equals("ADMIN")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // header에 email, role 등 payload 세팅
            // X를 붙여 custom header라는 것을 표현(관례적으로 사용되는 방식)
            // 추후 서비스 모듈에서 RequestHeader 어노테이션을 사용하여 아래 헤더를 꺼내 쓸 수 있음
            ServerWebExchange newExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Email", email)
                            .header("X-User-Role", role))
                    .build();
            return chain.filter(newExchange);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
