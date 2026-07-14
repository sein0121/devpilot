package com.devpilot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devpilot.domain.AuthProvider;
import com.devpilot.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
