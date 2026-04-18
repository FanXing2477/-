package org.example.fanxing.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.validation.Valid;
import org.example.fanxing.dto.RegisterDto;
import org.example.fanxing.dto.ResetPasswordDto;
import org.example.fanxing.entity.Result;
import org.example.fanxing.entity.User;
import org.example.fanxing.service.UserService;
import org.example.fanxing.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDto registerDto) {
        return userService.registerUser(registerDto);
    }

    @PostMapping("/login")
    public Result login(@Valid @RequestBody User user, BindingResult bindingResult) {
        // 1. 校验不通过，直接返回错误
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            String msg = "参数校验不通过";
            if (fieldError != null) {
                msg = fieldError.getDefaultMessage();
            }
            return Result.error(msg);
        }

        // 2. 二选一校验（phone 或 email 必须传一个）
        if ((user.getPhone() == null || user.getPhone().isBlank())
                && (user.getEmail() == null || user.getEmail().isBlank())) {
            return Result.error("手机号或邮箱必须填写一个");
        }
        return userService.loginUser(user);
    }

    @PutMapping("/change_password")
    public Result changePassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        return userService.changePassword(resetPasswordDto);
    }
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("token") String token){
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            return userService.getUserInfo(userId);
        } catch (JWTVerificationException e) {
            return Result.error("token无效，请重新登录");
        }
    }

}
