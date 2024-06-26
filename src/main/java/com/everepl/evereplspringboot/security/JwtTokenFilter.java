package com.everepl.evereplspringboot.security;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtTokenFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                // 토큰 유효성 검사 및 클레임 추출
                Claims claims = userService.validateTokenAndExtractClaims(token);

                // 클레임에서 사용자 ID 추출
                String userId = claims.getSubject();

                // 사용자 정보 조회
                User tokenUser = userService.findUserById(userId);

                // 사용자 엔티티에서 권한 정보 추출
                List<GrantedAuthority> authorities = tokenUser.getRoles().stream()
                        .map(User.Role::name)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 새로운 Authentication 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities);

                // SecurityContext에 Authentication 객체 저장
                SecurityContext newContext = SecurityContextHolder.createEmptyContext();
                newContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(newContext);
            } catch (Exception e) {
                // 토큰 검증 실패 처리
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }


}