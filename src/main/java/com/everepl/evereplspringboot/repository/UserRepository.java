package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByProviderAndProviderId(String provider, String providerId);
}