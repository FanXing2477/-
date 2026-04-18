package org.example.fanxing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.fanxing.entity.NoteSharePermission;

@Mapper
public interface NoteSharePermissionMapper extends BaseMapper<NoteSharePermission> {

    NoteSharePermission existsPermission(@Param("noteId") Long noteId,
                                 @Param("userId") Long userId);


}
