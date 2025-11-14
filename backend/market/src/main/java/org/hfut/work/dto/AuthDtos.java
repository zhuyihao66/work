package org.hfut.work.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AuthDtos {
    @Data
    public static class RegisterRequest {
        @Email
        @NotBlank
        private String email;

        @Size(min = 6, max = 64)
        @NotBlank
        private String password;

        @NotBlank
        private String displayName;

        private String phone;
        private String avatarUrl;
    }

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private Long userId;
        private String email;
        private String displayName;
        private String role;
    }
}


