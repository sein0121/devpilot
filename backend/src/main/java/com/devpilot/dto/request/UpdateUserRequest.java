package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class UpdateUserRequest {

    @NotBlank
    @Size(max = 50)
    private String nickname;
}
