package org.example.fanxing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.fanxing.entity.Tag;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag>{
    List<String> selectTagNamesByNoteId(@Param("noteId") Long noteId);
}
