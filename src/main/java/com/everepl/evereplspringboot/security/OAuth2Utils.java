package com.everepl.evereplspringboot.security;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

public class OAuth2Utils {
    public static String extractProviderId(OAuth2User oAuth2User, String provider) {
        switch (provider) {
            case "google":
            case "kakao":
                return oAuth2User.getAttribute("sub");
            case "naver":
                return Optional.ofNullable(oAuth2User.getAttribute("response"))
                        .map(response -> ((Map<String, Object>) response).get("id"))
                        .map(Object::toString)
                        .orElseThrow(() -> new RuntimeException("Naver response object is missing or does not contain an ID"));
            default:
                throw new RuntimeException("Unsupported provider: " + provider);
        }
    }
}
