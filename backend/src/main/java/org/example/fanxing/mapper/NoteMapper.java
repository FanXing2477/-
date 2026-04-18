package org.example.fanxing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.fanxing.entity.Note;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    //根据多个标签名分页查询笔记
    IPage<Note> selectNotesByTagNames(
            IPage<Note> page,
            @Param("userId") Integer userId,
            @Param("tagNames") List<String> tagNames
    );
}
