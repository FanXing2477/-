package org.example.fanxing.vo;

import lombok.Data;
import org.example.fanxing.entity.AiAnalysis;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDetailVo {
    // 笔记信息
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 标签列表
    private List<String> tagNameList;

    // AI 分析结果（没有则为 null）
    private AiAnalysis aiAnalysis;
}
