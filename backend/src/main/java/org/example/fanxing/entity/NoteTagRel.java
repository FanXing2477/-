package org.example.fanxing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note_tag_rel")
public class NoteTagRel {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private Long tagId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}