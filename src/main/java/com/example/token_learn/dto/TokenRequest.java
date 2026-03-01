package com.example.token_learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @NotBlank(message = "usernameは必須です")
    private String username;

    @NotBlank(message = "passwordは必須です")
    private String password;
}
