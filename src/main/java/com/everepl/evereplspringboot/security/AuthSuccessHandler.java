package com.everepl.evereplspringboot.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private JwtUtils jwtUtils;

    public AuthSuccessHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String token = jwtUtils.generateToken(authentication);

        String script = "<script>window.opener.postMessage({ token: '" + token + "' }, '*');window.close();</script>";

        response.setContentType("text/html");
        response.getWriter().write(script);
    }

}