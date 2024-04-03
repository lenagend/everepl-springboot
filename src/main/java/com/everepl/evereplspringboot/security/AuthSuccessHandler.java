package com.everepl.evereplspringboot.security;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private JwtUtils jwtUtils;
    private UserRepository userRepository;

    public AuthSuccessHandler(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            User user = findUserFromToken((OAuth2AuthenticationToken) authentication);

            // 사용자 정보를 기반으로 JWT 토큰 생성
            String token = jwtUtils.generateTokenWithUserInfo(user);

            String script = "<script>window.opener.postMessage({ token: '" + token + "' }, '*');window.close();</script>";

            response.setContentType("text/html");
            response.getWriter().write(script);
        }
    }

    private User findUserFromToken(OAuth2AuthenticationToken oauthToken) {
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = OAuth2Utils.extractProviderId(oauthToken.getPrincipal(), provider);

        // UserRepository를 사용하여 사용자 정보 조회
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with provider: " + provider + " and providerId: " + providerId));
    }

}