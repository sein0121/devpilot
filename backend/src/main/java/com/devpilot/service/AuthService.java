package com.devpilot.service;

import com.devpilot.dto.response.UserResponse;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(DevPilotOAuth2User principal) {
        return userRepository.findById(principal.getUserId())
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(principal.getUserId()));
    }
}