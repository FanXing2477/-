package org.example.fanxing.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$")
    private String email;
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;
    @Pattern(regexp = "^[a-zA-Z0-9]{6,12}$")
    private String password;
    private String nickname;
    private String avatarUrl;
    private String motto;
    private Integer status;
    // 创建时间（插入时自动填充）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    // 更新时间（插入和更新时都自动填充）
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}