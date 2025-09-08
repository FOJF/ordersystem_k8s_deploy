package com.study.odersystem.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final String[] patterns = {"/product/**", "/member/create", "/member/doLogin", "/member/refresh-at",
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "swagger-ui.html"};


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
