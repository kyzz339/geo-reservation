package com.jaehyun.demo.config;

import com.jaehyun.demo.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 공통 허용 경로 (로그인 없이 접근 가능)
                        .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/store/storeList", "/store/viewStore/**").permitAll()

                        // 2. 사장님(OWNER) 전용 경로
                        .requestMatchers("/store/myStore", "/store/createStore", "/store/manage/**", "/store/reservation/**").hasRole("OWNER")
                        .requestMatchers("/store/updateStore", "/store/deleteStore/**", "/store/viewMyStore").hasRole("OWNER")

                        // 3. 손님(USER) 전용 경로
                        .requestMatchers("/reservation/myReservation", "/reservation/my", "/reservation/**").hasRole("USER")

                        // 4. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler()) // 권한 부족 (403)
                        .authenticationEntryPoint(authenticationEntryPoint()) // 미인증 (401)
                );

        return http.build();
    }

    // 권한 부족 시 (로그인은 했으나 역할이 다를 때)
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // 무한 루프 방지를 위해 현재 페이지가 메인이 아닐 때만 리다이렉트
            if (!"/".equals(request.getRequestURI())) {
                response.sendRedirect("/");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        };
    }

    // 미인증 사용자 접근 시 (로그인 안 했을 때)
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            // 로그인 안 한 유저가 보호된 페이지 접근 시 메인으로 리다이렉트
            if (!"/".equals(request.getRequestURI())) {
                response.sendRedirect("/");
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder(secretKey, 16, 185000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512));
        encoders.put("argon2", new Argon2PasswordEncoder(16, 32, 1, 4096, 3));
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
