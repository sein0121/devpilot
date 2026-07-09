package com.devpilot.service;

import com.devpilot.domain.User;
import com.devpilot.dto.request.CreateUserRequest;
import com.devpilot.dto.request.UpdateUserRequest;
import com.devpilot.global.exception.DuplicateEmailException;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        CreateUserRequest request = new CreateUserRequest();
        setField(request, "email", "test@devpilot.com");
        setField(request, "nickname", "devuser");

        given(userRepository.existsByEmail("test@devpilot.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setField(user, "id", 1L);
            return user;
        });

        var response = userService.createUser(request);

        assertThat(response.getEmail()).isEqualTo("test@devpilot.com");
        assertThat(response.getNickname()).isEqualTo("devuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail() {
        CreateUserRequest request = new CreateUserRequest();
        setField(request, "email", "dup@devpilot.com");
        setField(request, "nickname", "dupuser");

        given(userRepository.existsByEmail("dup@devpilot.com")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getUser_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_success() {
        User user = new User("test@devpilot.com", "old");
        setField(user, "id", 1L);

        UpdateUserRequest request = new UpdateUserRequest();
        setField(request, "nickname", "new");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        var response = userService.updateUser(1L, request);

        assertThat(response.getNickname()).isEqualTo("new");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}