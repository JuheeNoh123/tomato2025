package com.sku_likelion.Moving_Cash_back.security;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.exception.InvalidUserException;
import com.sku_likelion.Moving_Cash_back.repository.UserRepository;
import com.sku_likelion.Moving_Cash_back.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer 토큰이 아니면 다음 필터로
        if(token == null || !token.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String checkToken = token.substring(7);

        try{
            jwtUtility.validateJwt(checkToken); //토큰 유효성 검증

            Claims claims = jwtUtility.getClaimsFromJwt(checkToken);
            String userId = claims.getSubject();

            //UserDetails userDetails = userService.loadUserByUserId(userId);
            User user = userRepository.findByUserId(userId).orElseThrow(() -> new InvalidUserException("잘못된 사용자 Id"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
        }catch(ExpiredJwtException e){
            writeUnauthorized(response, 401, "TOKEN_EXPIRED", "JWT가 만료되었습니다.");
        }catch (JwtException e){
            writeUnauthorized(response, 401, "TOKEN_INVALID", "유효하지 않은 JWT입니다.");
        }catch (Exception e){
            e.printStackTrace();
            writeUnauthorized(response, 401, "AUTH_ERROR", "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, int status, String code, String message) throws IOException{
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = String.format("{\"code\":\"%s\",\"message\":\"%s\"}", code, message);
        response.getWriter().write(body);
    }
}
