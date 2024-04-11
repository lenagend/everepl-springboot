package com.everepl.evereplspringboot.security;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtTokenFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                String userId = userService.getUserIdFromToken(token);

                // 단순히 사용자 ID를 기반으로 Authentication 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId, // 주체(principal)로 사용자 ID 사용
                        null,   // 자격증명(credentials)은 필요 없으므로 null
                        Collections.emptyList() // 권한(authorities)은 비워 둠
                );

                // SecurityContext에 Authentication 객체 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 토큰 검증 실패 처리
            SecurityContextHolder.clearContext(); // Context 초기화
        }

        filterChain.doFilter(request, response);
    }
}