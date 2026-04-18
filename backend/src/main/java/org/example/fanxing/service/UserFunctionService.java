package org.example.fanxing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.fanxing.dto.NoteUpdateDto;
import org.example.fanxing.entity.AiAnalysis;
import org.example.fanxing.entity.Note;
import org.example.fanxing.entity.Result;
import org.example.fanxing.entity.Tag;
import org.example.fanxing.vo.FriendRequestVo;
import org.example.fanxing.vo.FriendVo;
import org.example.fanxing.vo.NoteDetailVo;
import org.example.fanxing.vo.UserVo;

import java.util.List;
import java.util.Map;

public interface UserFunctionService {
    //3.1.2笔记管理
    //新增笔记
    Result createANote(Note note,String tags);
    //新建标签
    Tag createTag(Long userId, String tagName);
    //详情：查看单条笔记的完整内容及 AI 智能分析结果
    NoteDetailVo getNoteDetail(Long noteId, Long userId);
    //ai分析接口
    AiAnalysis aiAnalyze(Long noteId, Long userId);
    /*删除：采⽤逻辑删除*/
    //1、删除用户
    void deleteUser(Long userId);
    //2、删除笔记
    void deleteNote(Long noteId, Long userId);
    //好友删除：直接从好友关系表中删除
    //浏览记录的删除
    Result deleteHistory(Long noteId, Long userId);
    //标签功能：每篇笔记可以携带标签
    void updateTag(NoteUpdateDto noteUpdateDto,Long userId);
    //添加历史记录
    void addHistory(Long userId, Long noteId);
    //历史记录：查看⾃⼰最近看过的笔记
    Page<Note> getHistory(Long userId, Integer current, Integer size);
    //根据时间倒叙来分页查询笔记
    Page<Note> selectPageByTimeDesc(Integer userId,Integer current, Integer size);
    //根据标签来分页查询笔记
    Page<Note> pageByTagNames(Integer userId,Integer current, Integer size, String tagNames);
    //修改笔记
    void updateNote(NoteUpdateDto dto, Long userId);
    //可查看权限
    Map<String, Object> canSeeNote(Long noteId, Long currentUserId);
    //可编辑权限
    boolean canEditNote(NoteUpdateDto dto, Long currentUserId);
    // 设置笔记权限范围
    void setNoteVisibility(Long noteId, Long userId, Integer visibility);

    // 分享给指定好友
    void shareNoteToUser(Long noteId, Long userId, Long targetUserId, Integer permissionType);
    /*好友功能*/
    //根据 邮箱/手机号查找用户
    UserVo searchUser(String email, String phone);
    //发送好友申请
    void addFriendRequest(Long userId, Long toUserId);
    //查看我收到的好友申请
    List<FriendRequestVo> getMyRequests(Long userId);
    //处理好友申请
    void handleRequest(Long userId, Long requestId, Integer status);
    //查看我的好友列表（根据标签分组）
    List<FriendVo> getMyFriends(Long userId);
    //修改好友分组
    void updateGroup(Long userId, Long friendId, String groupName);
    //更改密码
    Result updatePassword(Long userId, String oldPassword, String newPassword, String confirmPassword);
    //获取好友笔记
    List<Map<String,Object>> getFriendNotes(Long userId, Long friendId);
}