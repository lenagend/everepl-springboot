package com.everepl.evereplspringboot.security;

import com.everepl.evereplspringboot.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final String SECRET_KEY = "c29mdGx5Y3VyaW91c3Byb2JhYmx5b3BpbmlvbnllYXJhaXJkZXNrcXVpY2tseWxhd2M=";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 생성
    public String generateTokenWithUserInfo(User user) {

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("name", user.getName()) // 사용자 이름 클레임 추가
                .claim("imageUrl", user.getImageUrl())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 3600000))
                .signWith(this.getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
