package org.example.fanxing.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendVo {
    private Long friendId;
    private String friendName;
    private String groupName;
    private String avatarUrl;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
