package org.example.fanxing.dto;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String email;
    private String phone;
    private String newPassword;
    private String confirmPassword;
}
