package org.example.fanxing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.example.fanxing.dto.RegisterDto;
import org.example.fanxing.dto.ResetPasswordDto;
import org.example.fanxing.entity.User;
import org.example.fanxing.mapper.UserMapper;
import org.example.fanxing.service.UserService;
import org.example.fanxing.util.JwtUtil;
import org.example.fanxing.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.fanxing.entity.Result;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

       @Override
    public boolean isExistUser(User user){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", user.getPhone());
        wrapper.or();
        wrapper.eq("email", user.getEmail());
        User checkUser = userMapper.selectOne(wrapper);
        if(checkUser == null){
            return true;
        }
        return false;
    };

    @Override
    @Transactional
    public Result registerUser(RegisterDto registerDto) {
        String username = registerDto.getUsername();
        String password = registerDto.getPassword();
        String confirmPassword = registerDto.getConfirmPassword();
        String phone = registerDto.getPhone();
        String email = registerDto.getEmail();
        User user = new User();
        user.setPhone(phone);
        user.setEmail(email);

        if (username == null || username.isEmpty()){
            return Result.error("用户名不能为空");
        }
        if (phone == null || phone.isEmpty()) {
            return Result.error("手机号不能为空");
        } else{
            String regex = "^1[3-9]\\d{9}$";
            if (!phone.matches(regex)) {
                return Result.error("手机号格式不正确");
            }
        }
        if (email == null || email.isEmpty()) {
            return Result.error("邮箱不能为空");
        } else{
            String regex = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
            if (!email.matches(regex)) {
                return Result.error("邮箱格式不正确");
            }
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        } else {
            String regex = "^[a-zA-Z0-9]{6,12}$";
            if (!password.matches(regex)) {
                return Result.error("密码格式不正确");
            }
        }
        if (!password.equals(confirmPassword)) {
            return Result.error("输入和再次输入的密码不一致");
        }
        boolean flag = isExistUser(user);
        if (flag == false) {
            return Result.error("用户已存在");
        }
        user.setUsername(username);
        String encodedPwd = PasswordUtil.encode(password);
        user.setPassword(encodedPwd);
        System.out.println(user);
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    @Override
    @Transactional
    public Result loginUser(User user){
        if (isExistUser(user) == false) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", user.getPhone());
            wrapper.or();
            wrapper.eq("email", user.getEmail());
            User checkUser = userMapper.selectOne(wrapper);
            if (PasswordUtil.match(user.getPassword(), checkUser.getPassword())) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("username", checkUser.getUsername());
                claims.put("phone", checkUser.getPhone());
                claims.put("email", checkUser.getEmail());
                claims.put("id", checkUser.getId());
                claims.put("nickname", checkUser.getNickname());
                claims.put("avatar_url", checkUser.getAvatarUrl());
                claims.put("motto", checkUser.getMotto());
                String token = JwtUtil.getToken(claims);
                return Result.success(token);
            }
        }
        return Result.error("账号或密码有误，请重新输入");
    }

    @Override
    @Transactional
    public Result changePassword(ResetPasswordDto resetPasswordDto){
        if (resetPasswordDto.getNewPassword() == null || resetPasswordDto.getNewPassword().isEmpty()) {
            return Result.error("密码不能为空");
        } else {
            String regex = "^[a-zA-Z0-9]{6,12}$";
            if (!resetPasswordDto.getNewPassword().matches(regex)) {
                return Result.error("密码格式不正确");
            }
        }
        if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getConfirmPassword())) {
            return Result.error("输入和再次输入的密码不一致");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", resetPasswordDto.getPhone()).or().eq("email", resetPasswordDto.getEmail());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("您的手机号或邮箱不存在");
        }

        String encodedPwd = PasswordUtil.encode(resetPasswordDto.getNewPassword());
        user.setPassword(encodedPwd);
        userMapper.updateById(user);
        return Result.success("密码修改成功");
    }

    @Override
    public Result getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar_url", user.getAvatarUrl());
        userInfo.put("motto", user.getMotto());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());

        return Result.success(userInfo);
    }
}
