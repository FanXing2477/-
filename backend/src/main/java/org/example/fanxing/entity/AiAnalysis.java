package org.example.fanxing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_analysis")
public class AiAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private Long userId;
    private String summary;
    private String keyPoints;
    private String suggestedTags;
    private String fullResponse;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}