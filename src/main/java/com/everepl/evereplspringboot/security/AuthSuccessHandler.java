package com.everepl.evereplspringboot.security;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    public AuthSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            User user = userService.findUserByOAuthToken((OAuth2AuthenticationToken) authentication);

            // 사용자 정보를 기반으로 JWT 토큰 생성
            String token = userService.generateTokenForUser(user);

            String script = "<script>window.opener.postMessage({ token: '" + token + "' }, '*');window.close();</script>";

            response.setContentType("text/html");
            response.getWriter().write(script);
        }
    }



}