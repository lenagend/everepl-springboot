package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.*;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Report;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.exceptions.AlreadyExistsException;
import com.everepl.evereplspringboot.exceptions.UserActionRestrictionException;
import com.everepl.evereplspringboot.repository.ReportRepository;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Value("${admin.provider}")
    private String adminProvider;

    @Value("${admin.providerId}")
    private String adminProviderId;

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public User findUserById(String userId) {
        Long id = Long.parseLong(userId);
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public UserResponse getUserByUserId(Long userId) {
        return userRepository.findById(userId).map(this::toDto)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName()); // 인증된 사용자의 고유 ID 추출

        return findUserById(userId);
    }

    public User findUserByOAuthToken(OAuth2AuthenticationToken oauthToken) {
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = OAuth2Utils.extractProviderId(oauthToken.getPrincipal(), provider);

        // UserRepository를 사용하여 사용자 정보 조회
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with provider: " + provider + " and providerId: " + providerId));
    }

    /**
     * JWT 토큰을 검증하고 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 클레임
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    public Claims validateTokenAndExtractClaims(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 접두어 제거
            return jwtUtils.extractAllClaims(token); // 토큰에서 클레임 추출
        } else {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
    }

    /**
     * 사용자 ID를 기반으로 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public User fetchUserById(String userId) {
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    /**
     * JWT 토큰을 검증하고, 해당 사용자의 정보를 조회합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출된 사용자 객체
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public User verifyTokenAndFetchUser(String token) {
        Claims claims = validateTokenAndExtractClaims(token); // 토큰 검증 및 클레임 추출
        String userId = claims.getSubject(); // 클레임에서 사용자 ID 추출
        return fetchUserById(userId); // 사용자 ID로 사용자 정보 조회
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

                    // 권한 리스트를 저장할 Set 생성
                    Set<User.Role> roles = new HashSet<>();

                    // 관리자 여부 확인
                    if (provider.equals(adminProvider) && providerId.equals(adminProviderId)) {
                        roles.add(User.Role.ROLE_ADMIN);
                    } else {
                        roles.add(User.Role.ROLE_USER);
                    }

                    newUser.setRoles(roles);

                    userRepository.save(newUser);  // 새로운 사용자 저장
                    return newUser;
                });
    }

    public UserResponse toDto(User user){
        Set<String> roleNames = user.getRoles().stream()
                .map(User.Role::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getImageUrl(),
                user.getProvider(),
                roleNames,
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

        // 프로필 사진 변경 금지 기간 검사
        if (userRequest.getImageUrl() != null && currentUser.getProfilePictureBanUntil() != null
                && LocalDateTime.now().isBefore(currentUser.getProfilePictureBanUntil())) {
            throw new UserActionRestrictionException("신고누적으로 다음 기간까지 프로필을 변경하실 수 없습니다. " + currentUser.getProfilePictureBanUntil());
        }

        Optional.ofNullable(userRequest.getImageUrl()).ifPresent(currentUser::setImageUrl);
        Optional.ofNullable(userRequest.getNotificationSetting()).ifPresent(currentUser::setNotificationSetting);

        userRepository.save(currentUser); // 변경사항 저장
        return toDto(currentUser);
    }


}
