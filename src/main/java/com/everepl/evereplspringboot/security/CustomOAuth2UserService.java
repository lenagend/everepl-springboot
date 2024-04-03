package com.everepl.evereplspringboot.security;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = OAuth2Utils.extractProviderId(oAuth2User, provider);

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 사용자가 데이터베이스에 없는 경우, 새로운 사용자 생성
                    User newUser = new User();
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    return newUser;
                });

        // 사용자 정보 저장
        userRepository.save(user);

        return oAuth2User;
    }

}
