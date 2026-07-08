package com.devpilot.dto.response;

import java.time.LocalDateTime;

import com.devpilot.domain.User;

import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String email;
    private final String nickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    private UserResponse(Long id, String email, String nickname, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(), 
            user.getEmail(), 
            user.getNickname(), 
            user.getCreatedAt(), 
            user.getUpdatedAt()
        );
    }
}
