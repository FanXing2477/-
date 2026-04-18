package org.example.fanxing.service;

import org.example.fanxing.dto.RegisterDto;
import org.example.fanxing.dto.ResetPasswordDto;
import org.example.fanxing.entity.Result;
import org.example.fanxing.entity.User;

public interface UserService {
    //查询用户是否存在
    boolean isExistUser(User user);
    //注册
    Result registerUser(RegisterDto registerDto);
    //登录
    Result loginUser(User user);
    //修改密码
    Result changePassword(ResetPasswordDto resetPasswordDto);
    //获取用户
    Result getUserInfo(Long userId);
}
