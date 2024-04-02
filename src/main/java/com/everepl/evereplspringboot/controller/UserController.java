package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.UserRepository;
import com.everepl.evereplspringboot.security.OAuth2Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // 제공자 이름과 사용자의 고유 ID를 사용하여 사용자 정보 조회
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = OAuth2Utils.extractProviderId(oAuth2User, provider);


        // 데이터베이스에서 사용자 조회
        Optional<User> userOpt = userRepository.findByProviderAndProviderId(provider, providerId);

        // 사용자가 존재하지 않을 경우 예외 발생
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException(""));

        return user;
    }
}
