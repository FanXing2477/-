package org.example.fanxing.dto;

import lombok.Data;

import java.util.List;

@Data
public class NoteUpdateDto {
    private Long noteId;
    private String title;
    private String content;

    private List<Long> tagIds;

}
