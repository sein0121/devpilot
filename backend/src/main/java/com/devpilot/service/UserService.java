package com.devpilot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devpilot.domain.User;
import com.devpilot.dto.request.CreateUserRequest;
import com.devpilot.dto.request.UpdateUserRequest;
import com.devpilot.dto.response.UserResponse;
import com.devpilot.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Request DTO -> Entity 변환
        User user = new User(request.getEmail(), request.getNickname());
        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
        .stream()
        .map(UserResponse::from) // .map(user -> UserResponse.from(user))
        .toList();
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
        user.updateNickname(request.getNickname());
        return UserResponse.from(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
        userRepository.delete(user);
    }
    
}
