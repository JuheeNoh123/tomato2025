package com.sku_likelion.Moving_Cash_back.config;

import com.sku_likelion.Moving_Cash_back.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import java.nio.charset.StandardCharsets;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"
                    .getBytes(StandardCharsets.UTF_8));
        };
    }

//    @Bean
//    public AccessDeniedHandler accessDeniedHandler(){
//        return (request, response, accessDeniedException) -> {
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setContentType("application/json;charset=UTF-8");
//            response.getOutputStream().write("{\"code\":\"FORBIDDEN\",\"message\":\"접근 권한이 없습니다.\"}"
//                    .getBytes(StandardCharsets.UTF_8));
//        };
//    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .cors(Customizer.withDefaults()) // spring boot의 기본 CORS설정 사용
                .csrf(AbstractHttpConfigurer::disable)  //공격 방지 기능 비활성화
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 아예 만들지 않도록 설정
                .exceptionHandling(ex -> ex // 인증,인가 실패시 핸들러 지정
                        .authenticationEntryPoint(authenticationEntryPoint()) //인증이 안된 사용자가 보호된 API 접근시 401응답 주는 로직
//                        .accessDeniedHandler(accessDeniedHandler()) // 인증은 되었지만 권한이 없는 사용자가 접근시 403응답을 주는 로직
            )
                .authorizeHttpRequests(auth -> auth // 엔드포인트별 접근 권한 설정
                        .requestMatchers("/auth/**","/user/signup", "/user/login", "/error").permitAll() // 누구나 접근 가능한 API 경로
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()  //Swagger 문서관련 누구나 접근가능
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight 요청은 누구나 접근가능
                        .anyRequest().authenticated() //나머지는 JWT 인증 필요
                )
                //Spring Security의 기본 UsernamePasswordAuthenticationFilter 전에 JWT 인증 필터를 실행하도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
