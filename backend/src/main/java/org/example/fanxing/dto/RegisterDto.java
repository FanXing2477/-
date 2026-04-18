package org.example.fanxing.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String phone;
    private String password;
    private String confirmPassword;
    private String email;
    private String username;
}
