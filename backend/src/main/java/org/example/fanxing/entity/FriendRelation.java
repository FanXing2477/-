package org.example.fanxing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend_relation")
public class FriendRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long friendId;
    private String groupName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}