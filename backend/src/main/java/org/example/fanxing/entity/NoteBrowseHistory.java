package org.example.fanxing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note_browse_history")
public class NoteBrowseHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long noteId;
    private LocalDateTime browseTime;
}