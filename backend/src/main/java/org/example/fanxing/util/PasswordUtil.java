package org.example.fanxing.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 1. 注册/修改密码时：加密存储
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 2. 登录时：验证匹配
    public static boolean match(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

}