package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.UserRequest;
import com.everepl.evereplspringboot.dto.UserResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.exceptions.AlreadyExistsException;
import com.everepl.evereplspringboot.repository.UserRepository;
import com.everepl.evereplspringboot.security.JwtUtils;
import com.everepl.evereplspringboot.security.OAuth2Utils;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Value("${default.profile.image.url}")
    private String defaultProfileImageUrl;

    public UserService(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public UserResponse getUserByUserId(Long userId) {
        return userRepository.findById(userId).map(this::toDto)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName()); // 인증된 사용자의 고유 ID 추출

        // userRepository를 사용하여 User 객체 조회
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public User findUserByOAuthToken(OAuth2AuthenticationToken oauthToken) {
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = OAuth2Utils.extractProviderId(oauthToken.getPrincipal(), provider);

        // UserRepository를 사용하여 사용자 정보 조회
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with provider: " + provider + " and providerId: " + providerId));
    }

    public User verifyTokenAndFetchUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 접두어 제거
            Claims claims = jwtUtils.extractAllClaims(token); // 토큰에서 클레임 추출
            String userId = claims.getSubject(); // 클레임에서 사용자 ID 추출

            // userRepository를 사용하여 User 객체 조회
            return userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        } else {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
    }

    public String generateTokenForUser(User user) {
        // 사용자 정보를 기반으로 JWT 토큰 생성
        return jwtUtils.generateTokenWithUserInfo(user);
    }

    public String getUserIdFromToken(String token) {
        Claims claims = jwtUtils.extractAllClaims(token); // 토큰에서 클레임 추출
        return claims.getSubject(); // 클레임에서 사용자 ID 추출
    }

    @Transactional
    public User loadOrCreateUser(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 사용자가 데이터베이스에 없는 경우, 새로운 사용자 생성
                    User newUser = new User();
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    newUser.setImageUrl(defaultProfileImageUrl);
                    userRepository.save(newUser);  // 새로운 사용자 저장
                    return newUser;
                });
    }

    public UserResponse toDto(User user){
        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getImageUrl(),
                user.getProvider(),
                user.isNotificationSetting()
        );
    }

    public UserResponse updateUser(UserRequest userRequest) {
        User currentUser = getAuthenticatedUser();

        // 닉네임 변경 시 중복 체크
        Optional.ofNullable(userRequest.getName()).ifPresent(newName -> {
            if (userRepository.existsByName(newName) && !newName.equals(currentUser.getName())) {
                throw new AlreadyExistsException("이미 사용 중인 닉네임입니다.");
            }
            currentUser.setName(newName);
        });

        Optional.ofNullable(userRequest.getImageUrl()).ifPresent(currentUser::setImageUrl);
        Optional.ofNullable(userRequest.getNotificationSetting()).ifPresent(currentUser::setNotificationSetting);

        userRepository.save(currentUser); // 변경사항 저장
        return toDto(currentUser);
    }

}
