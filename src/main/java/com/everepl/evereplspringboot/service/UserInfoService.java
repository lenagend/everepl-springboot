package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {

    private final UserRepository userRepository;

    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName()); // 인증된 사용자의 고유 ID 추출

        // userRepository를 사용하여 User 객체 조회
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }
}
