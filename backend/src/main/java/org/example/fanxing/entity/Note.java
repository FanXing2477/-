package org.example.fanxing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private Integer visibility;
    // 创建时间（插入时自动填充）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    // 更新时间（插入和更新时都自动填充）
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}