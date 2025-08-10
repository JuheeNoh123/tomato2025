package com.sku_likelion.Moving_Cash_back.security;

import com.sku_likelion.Moving_Cash_back.exception.HandleJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtUtility {

    private final SecretKey secretKey; // JWT 서명에 사용되는 비밀키

    private static final long expirationTime = 1000 * 60 * 60; // JWT 만료시간

    //JWT 서명에 사용되는 비밀키 생성
    public JwtUtility(@Value("${jwt.base64Secret}") String base64Secret){
        String trimmed = base64Secret == null ? "" : base64Secret.trim();
        byte[] keyBytes;
        try{
            keyBytes = Base64.getDecoder().decode(trimmed);
        }catch(IllegalArgumentException e){
            throw new IllegalStateException("jwt.base64Secret이 유효한 Base64 형식이 아닙니다.", e);
        }
        if (keyBytes.length < 32) { // HS256 최소 권장 256bit
            throw new IllegalStateException("jwt.base64Secret 키 길이가 너무 짧습니다. 최소 32바이트 이상 권장.");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    //JWT 토큰 생성
    public String generateJwt(String userId){
        Instant now = Instant.now();
        return Jwts.builder()
                .claims()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationTime)))
                .and()
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // JWT 토큰 유효성 검사
    public boolean validateJwt(String jwt){
        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt);
            return true;
        }catch (ExpiredJwtException e){
            throw e;
        }catch (UnsupportedJwtException e){
            throw new HandleJwtException("지원되지 않는 JWT형식");
        }catch (MalformedJwtException e){
            throw new HandleJwtException("손상된 JWT");
        }catch (SecurityException e){
            throw new HandleJwtException("서명이 올바르지 않은 JWT");
        }catch (IllegalArgumentException e){
            throw new HandleJwtException("JWT가 null이거나 빈 문자열임");
        }catch (JwtException e){
            throw new HandleJwtException("기타 JWT관련 예외");
        }
    }

    //JWT 토큰에서 클레임 추출
    public Claims getClaimsFromJwt(String jwt){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }


}
