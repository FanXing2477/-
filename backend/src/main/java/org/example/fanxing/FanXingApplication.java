package org.example.fanxing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.example.fanxing.mapper", markerInterface = BaseMapper.class)
public class FanXingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FanXingApplication.class, args);
    }

}
