package com.T82.coupon.utils;

import com.T82.coupon.global.domain.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtil {
    private final SecretKey secretKey;
    public UserDto parseToken(String token) {
        Claims payload = (Claims) Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parse(token)
                .getPayload();
        return UserDto.fromClaims(payload);
    }
    public JwtUtil(
            @Value("${token.secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }
}
