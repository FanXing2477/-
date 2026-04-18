package org.example.fanxing.config;

import org.example.fanxing.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //登录注册不拦截
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/user/register",
                "/user/login", "/user/change_password");
    }
}
