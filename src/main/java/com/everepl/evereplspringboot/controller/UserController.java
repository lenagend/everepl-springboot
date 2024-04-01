package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.UserRepository;
import com.everepl.evereplspringboot.service.CustomOAuth2UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        // 제공자 이름과 사용자의 고유 ID를 사용하여 사용자 정보 조회
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = oauthToken.getPrincipal().getAttribute("sub"); // 'sub'는 Google의 경우. 다른 제공자의 경우 해당 필드명을 확인해야 함

        User user = userRepository.findByProviderAndProviderId(provider, providerId);
        return user;
    }
}
