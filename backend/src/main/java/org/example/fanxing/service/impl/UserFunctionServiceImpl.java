package org.example.fanxing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.fanxing.dto.NoteUpdateDto;
import org.example.fanxing.entity.*;
import org.example.fanxing.mapper.*;
import org.example.fanxing.service.UserFunctionService;
import org.example.fanxing.util.NotePermissionUtil;
import org.example.fanxing.util.PasswordUtil;
import org.example.fanxing.vo.FriendRequestVo;
import org.example.fanxing.vo.FriendVo;
import org.example.fanxing.vo.NoteDetailVo;
import org.example.fanxing.vo.UserVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserFunctionServiceImpl implements UserFunctionService {

    private final ChatClient chatClient;

    public UserFunctionServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteBrowseHistoryMapper noteBrowseHistoryMapper;

    @Autowired
    private NoteTagRelMapper noteTagRelMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NoteSharePermissionMapper noteSharePermissionMapper;

    @Autowired
    private FriendRelationMapper friendRelationMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private AiAnalysisMapper aiAnalysisMapper;

    @Override
    @Transactional
    public Result createANote(Note note, String tags){
        noteMapper.insert(note);
        if (tags != null && !tags.isEmpty()){
            String[] tagArr = tags.split(",");
            for (String tag : tagArr){
                tag = tag.trim();
                if (tag.isEmpty()) continue;
                //根据tag名字，查询tag
                LambdaQueryWrapper<Tag> queryWrapper = Wrappers.<Tag>lambdaQuery();
                queryWrapper.eq(Tag::getName, tag)
                        .eq(Tag::getUserId, note.getUserId());
                Tag existTag = tagMapper.selectOne(queryWrapper);
                //如果不存在标签，则创建一个标签
                if (existTag == null){
                    existTag = new Tag();
                    existTag.setName(tag);
                    existTag.setUserId(note.getUserId());
                    tagMapper.insert(existTag);
                }
                //保存关联表
                NoteTagRel noteTagRel = new NoteTagRel();
                noteTagRel.setNoteId(note.getId());
                noteTagRel.setTagId(existTag.getId());
                noteTagRelMapper.insert(noteTagRel);
            }
        }
        return Result.success("成功创建一条笔记");
    }

    @Override
    @Transactional
    public Tag createTag(Long userId, String tagName){
        Tag tag = new Tag();

        tag.setUserId(userId);
        tag.setName(tagName);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public Page<Note> selectPageByTimeDesc(Integer userId,Integer current, Integer size){
        //构造分页对象
        Page<Note> page = new Page<>(current, size);
        //构造查询条件：按创建时间倒叙
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.eq(Note::getIsDeleted,0);
        wrapper.orderByDesc(Note::getCreateTime);
        return noteMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Note> pageByTagNames(Integer userId,Integer current, Integer size, String tagNames){
        Page<Note> page = new Page<>(current, size);
        //把前端传来的多个标签打包成数组
        List<String> tagNameList = Arrays.asList(tagNames.split(","));
        return (Page<Note>) noteMapper.selectNotesByTagNames(page,userId,tagNameList);
    }

    @Override
    @Transactional
    public void updateNote(NoteUpdateDto dto, Long userId) {

        Long noteId = dto.getNoteId();
        Note note = noteMapper.selectById(noteId);

        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }

        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改他人笔记");
        }

        Note updateNote = new Note();
        updateNote.setId(noteId);
        updateNote.setTitle(dto.getTitle());
        updateNote.setContent(dto.getContent());
        updateNote.setUpdateTime(LocalDateTime.now());

        noteMapper.updateById(updateNote);

    }

    @Override
    @Transactional
    public void updateTag(NoteUpdateDto NoteUpdateDto, Long userId) {
        Long noteId = NoteUpdateDto.getNoteId();
        Note note = noteMapper.selectById(noteId);

        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无法为他人笔记更改标签");
        }

        // 删除这篇笔记原来的所有标签关联
        LambdaQueryWrapper<NoteTagRel> delWrapper = Wrappers.lambdaQuery();
        delWrapper.eq(NoteTagRel::getNoteId, noteId);
        noteTagRelMapper.delete(delWrapper);

        // 前端传了标签，批量插入新关联
        List<Long> tagIds = NoteUpdateDto.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<NoteTagRel> relList = new ArrayList<>();
            for (Long tagId : tagIds) {
                NoteTagRel rel = new NoteTagRel();
                rel.setNoteId(noteId);
                rel.setTagId(tagId);
                relList.add(rel);
            }
            // 批量插入
            for (NoteTagRel rel : relList) {
                noteTagRelMapper.insert(rel);
            }
        }
    }

    @Override
    public Map<String,Object> canSeeNote(Long noteId, Long currentUserId){
        Note note = noteMapper.selectById(noteId);
        NoteSharePermission permission = noteSharePermissionMapper.existsPermission(noteId, currentUserId);

        if (!NotePermissionUtil.canView(currentUserId, note, permission)){
            throw new RuntimeException("无查看权限");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("id", note.getId());
        map.put("title", note.getTitle());
        map.put("content", note.getContent());
        map.put("userId", note.getUserId());
        map.put("visibility", note.getVisibility());
        map.put("createTime", note.getCreateTime());
        map.put("updateTime", note.getUpdateTime());
        return map;
    }

    @Override
    public boolean canEditNote(NoteUpdateDto dto, Long currentUserId){
        Note existNote = noteMapper.selectById(dto.getNoteId());
        NoteSharePermission permission = noteSharePermissionMapper.existsPermission(dto.getNoteId(), currentUserId);

        if (!NotePermissionUtil.canEdit(currentUserId, existNote, permission)){
            throw new RuntimeException("无编辑权限");
        }

        Note updateNote = new Note();
        updateNote.setId(dto.getNoteId());
        updateNote.setTitle(dto.getTitle());
        updateNote.setContent(dto.getContent());
        updateNote.setUpdateTime(LocalDateTime.now());

        return noteMapper.updateById(updateNote) > 0;
    }

    @Override
    @Transactional
    public void setNoteVisibility(Long noteId, Long userId, Integer visibility) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) throw new RuntimeException("笔记不存在");
        if (!note.getUserId().equals(userId)) throw new RuntimeException("无权限");

        Note update = new Note();
        update.setId(noteId);
        update.setVisibility(visibility);
        noteMapper.updateById(update);
    }

    @Override
    @Transactional
    public void shareNoteToUser(Long noteId, Long userId, Long targetUserId, Integer permissionType) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) throw new RuntimeException("笔记不存在");
        if (!note.getUserId().equals(userId)) throw new RuntimeException("无权限");

        // 先删旧权限
        LambdaQueryWrapper<NoteSharePermission> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(NoteSharePermission::getNoteId, noteId);
        wrapper.eq(NoteSharePermission::getUserId, targetUserId);
        noteSharePermissionMapper.delete(wrapper);

        // 插入新权限
        NoteSharePermission p = new NoteSharePermission();
        p.setNoteId(noteId);
        p.setUserId(targetUserId);
        p.setPermissionType(permissionType);
        noteSharePermissionMapper.insert(p);
    }

    @Override
    public void deleteNote(Long noteId, Long userId){
        Note note = noteMapper.selectById(noteId);

        // 1. 笔记不存在
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }

        // 2. 只能删自己的
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此笔记");
        }

        // 3. 逻辑删除（MP 自动变成 update set deleted=1）
        noteMapper.deleteById(noteId);

        /* 4. 同步删除关联表关系（必须手动！）
        ，关联表的连除功能只认物理上的删除，所以需要手动删除*/
        LambdaQueryWrapper<NoteTagRel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(NoteTagRel::getNoteId, noteId);
        noteTagRelMapper.delete(wrapper);

    }

    @Override
    @Transactional
    public void deleteUser(Long userId){
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 逻辑删除用户
        userMapper.deleteById(userId);
        // 逻辑删除该用户所有笔记
        LambdaQueryWrapper<Note> noteWrapper = Wrappers.lambdaQuery();
        noteWrapper.eq(Note::getUserId, userId);
        // 用 update 手动设置 deleted = 1
        Note noteUpdate = new Note();
        noteUpdate.setIsDeleted(1);
        noteMapper.update(noteUpdate, noteWrapper);

        // 3. 同步删除这些笔记对应的关联关系
        // 先查所有笔记ID
        List<Long> noteIds = noteMapper.selectObjs(noteWrapper
                        .select(Note::getId))
                .stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        if (!noteIds.isEmpty()) {
            LambdaQueryWrapper<NoteTagRel> relWrapper = Wrappers.lambdaQuery();
            relWrapper.in(NoteTagRel::getNoteId, noteIds);
            noteTagRelMapper.delete(relWrapper);
        }
    }

    @Override
    @Transactional
    public void addHistory(Long userId, Long noteId) {
        LambdaQueryWrapper<NoteBrowseHistory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(NoteBrowseHistory::getUserId, userId);
        wrapper.eq(NoteBrowseHistory::getNoteId, noteId);

        NoteBrowseHistory noteBrowseHistory = noteBrowseHistoryMapper.selectOne(wrapper);

        if (noteBrowseHistory != null) {
            //已存在，更新浏览时间
            noteBrowseHistory.setBrowseTime(LocalDateTime.now());
            noteBrowseHistoryMapper.updateById(noteBrowseHistory);
        }
        else{
            //不存在，新增一条浏览记录
            NoteBrowseHistory h = new NoteBrowseHistory();
            h.setBrowseTime(LocalDateTime.now());
            h.setNoteId(noteId);
            h.setUserId(userId);
            noteBrowseHistoryMapper.insert(h);
        }
    }

    @Override
    public Page<Note> getHistory(Long userId,Integer current,Integer size){
        Page<NoteBrowseHistory> page = new Page<>(current,size);

        LambdaQueryWrapper<NoteBrowseHistory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(NoteBrowseHistory::getUserId, userId);
        wrapper.orderByDesc(NoteBrowseHistory::getBrowseTime);

        noteBrowseHistoryMapper.selectPage(page, wrapper);

        List<Note> noteList = new ArrayList<>();

        // 获取原始记录列表
        List<NoteBrowseHistory> historyList = page.getRecords();

        // 循环遍历每一条历史记录
        for (NoteBrowseHistory history : historyList) {

            // 查询对应的笔记（过滤逻辑删除）
            Note note = noteMapper.selectOne(Wrappers.lambdaQuery(Note.class)
                    .eq(Note::getId, history.getNoteId())
                    .eq(Note::getIsDeleted, 0));

            if (note == null) {
                continue;
            }

            // 添加笔记进列表
            noteList.add(note);
        }

        Page<Note> notePage = new Page<>(current, size, page.getTotal());
        notePage.setRecords(noteList);
        return notePage;
    }

    @Override
    @Transactional
    public Result deleteHistory(Long noteId, Long userId){
        LambdaQueryWrapper<NoteBrowseHistory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(NoteBrowseHistory::getNoteId, noteId)
                .eq(NoteBrowseHistory::getUserId, userId);

        int delete = noteBrowseHistoryMapper.delete(wrapper);

        if (delete > 0){
            return Result.success("刪除成功");
        }else{
            return Result.error("记录s不存在");
        }
    }

    @Override
    public UserVo searchUser(String email, String phone){
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getEmail,email);
        wrapper.or().eq(User::getPhone,phone);

        wrapper.eq(User::getIsDeleted,0);

        User user = userMapper.selectOne(wrapper);
        if (user == null) return null;

        // 转 VO，不返回敏感信息
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional
    public void addFriendRequest(Long userId, Long toUserId){
        if (userId.equals(toUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        LambdaQueryWrapper<FriendRelation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRelation::getUserId, userId);
        wrapper.eq(FriendRelation::getFriendId, toUserId);
        Long count = friendRelationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("你们已经是好友了");
        }

        LambdaQueryWrapper<FriendRequest> wrapper2 = Wrappers.lambdaQuery();
        wrapper2.eq(FriendRequest::getFromUserId, userId);
        wrapper2.eq(FriendRequest::getToUserId, toUserId);
        wrapper2.eq(FriendRequest::getStatus,0);
        Long exist = friendRequestMapper.selectCount(wrapper2);
        if (exist > 0) {
            throw new RuntimeException("已发送过好友申请，请勿重复发送");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(userId);
        request.setToUserId(toUserId);
        request.setStatus(0);

        friendRequestMapper.insert(request);
    }

    @Override
    public List<FriendRequestVo> getMyRequests(Long userId){
        LambdaQueryWrapper<FriendRequest> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRequest::getToUserId, userId);
        wrapper.eq(FriendRequest::getStatus,0);
        wrapper.orderByDesc(FriendRequest::getCreateTime);

        List<FriendRequest> list = friendRequestMapper.selectList(wrapper);
        List<FriendRequestVo> voList = new ArrayList<>();

        for (FriendRequest request : list) {
            FriendRequestVo vo = new FriendRequestVo();
            BeanUtils.copyProperties(request, vo);

            //查询申请人学习
            User fromUser = userMapper.selectById(request.getFromUserId());
            if (fromUser != null) {
                vo.setFromUsername(fromUser.getUsername());
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional
    public void handleRequest(Long userId, Long requestId, Integer status){
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!request.getToUserId().equals(userId)) {
            throw new RuntimeException("无权限处理改申请");
        }
        if (!request.getStatus().equals(0)) {
            throw new RuntimeException("该申请已经处理过");
        }
        //更新申请状态
        request.setStatus(status);
        friendRequestMapper.updateById(request);

        // 如果是同意，就互相添加好友关系
        if (status == 1) {
            // A -> B
            FriendRelation r1 = new FriendRelation();
            r1.setUserId(request.getFromUserId());
            r1.setFriendId(request.getToUserId());
            r1.setGroupName("默认");
            r1.setCreateTime(LocalDateTime.now());
            friendRelationMapper.insert(r1);

            // B -> A
            FriendRelation r2 = new FriendRelation();
            r2.setUserId(request.getToUserId());
            r2.setFriendId(request.getFromUserId());
            r2.setGroupName("默认");
            r2.setCreateTime(LocalDateTime.now());
            friendRelationMapper.insert(r2);
        }
    }

    @Override
    public List<FriendVo> getMyFriends(Long userId){
        LambdaQueryWrapper<FriendRelation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRelation::getUserId, userId);
        wrapper.orderByAsc(FriendRelation::getGroupName);

        List<FriendRelation> list = friendRelationMapper.selectList(wrapper);
        List<FriendVo> voList = new ArrayList<>();

        for (FriendRelation relation : list) {
            FriendVo vo = new FriendVo();
            BeanUtils.copyProperties(relation, vo);

            User friend = userMapper.selectById(relation.getFriendId());
            if (friend != null) {
                vo.setAvatarUrl(friend.getAvatarUrl());
                vo.setFriendName(friend.getUsername());
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional
    public void updateGroup(Long userId, Long friendId, String groupName){
        LambdaQueryWrapper<FriendRelation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRelation::getUserId, userId);
        wrapper.eq(FriendRelation::getFriendId, friendId);

        FriendRelation relation = friendRelationMapper.selectOne(wrapper);
        if (relation == null) {
            throw new RuntimeException("你们还不是好友");
        }
        relation.setGroupName(groupName);
        friendRelationMapper.updateById(relation);
    }

    @Override
    public NoteDetailVo getNoteDetail(Long noteId,Long userId){
        // 1. 查询笔记（过滤已删除）
        Note note = noteMapper.selectOne(Wrappers.lambdaQuery(Note.class)
                .eq(Note::getId, noteId)
                .eq(Note::getIsDeleted, 0));

        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }

        // 2. 只能看自己的
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限查看该笔记");
        }

        // 3. 组装VO
        NoteDetailVo vo = new NoteDetailVo();
        BeanUtils.copyProperties(note, vo);

        // 4. 查询标签列表
        List<String> tagNames = tagMapper.selectTagNamesByNoteId(noteId);
        vo.setTagNameList(tagNames);

        // 5. 查询AI分析结果
        LambdaQueryWrapper<AiAnalysis> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiAnalysis::getNoteId, noteId);
        wrapper.eq(AiAnalysis::getUserId, userId);
        AiAnalysis ai = aiAnalysisMapper.selectOne(wrapper);
        vo.setAiAnalysis(ai);

        return vo;
    }

    @Override
    public AiAnalysis aiAnalyze(Long noteId, Long userId){
        // 1. 校验笔记
        Note note = noteMapper.selectOne(Wrappers.lambdaQuery(Note.class)
                .eq(Note::getId, noteId)
                .eq(Note::getIsDeleted, 0));

        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限分析此笔记");
        }

        // 2. 调用AI
        String fullResponse = null;
        try {
            String prompt = "请对以下笔记进行智能分析：\n" +
                    "标题：" + note.getTitle() + "\n" +
                    "内容：" + note.getContent() + "\n\n" +
                    "返回格式严格如下，不要多余内容：\n" +
                    "【摘要】\n" +
                    "【关键点】\n" +
                    "【建议标签】";

            fullResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("AI分析失败：" + e.getMessage());
        }

        // 3. 解析AI返回
        String summary = "";
        String keyPoints = "";
        String suggestedTags = "";

        String[] lines = fullResponse.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("【摘要】")) {
                summary = lines[++i].trim();
            } else if (line.startsWith("【关键点】")) {
                keyPoints = lines[++i].trim();
            } else if (line.startsWith("【建议标签】")) {
                suggestedTags = lines[++i].trim();
            }
        }

        // 4. 查询是否已有AI记录
        LambdaQueryWrapper<AiAnalysis> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiAnalysis::getNoteId, noteId);
        wrapper.eq(AiAnalysis::getUserId, userId);
        AiAnalysis exist = aiAnalysisMapper.selectOne(wrapper);

        if (exist != null) {
            // 已有 → 更新覆盖
            exist.setSummary(summary);
            exist.setKeyPoints(keyPoints);
            exist.setSuggestedTags(suggestedTags);
            exist.setFullResponse(fullResponse);
            aiAnalysisMapper.updateById(exist);
            return exist;
        } else {
            // 没有 → 新增
            AiAnalysis ai = new AiAnalysis();
            ai.setNoteId(noteId);
            ai.setUserId(userId);
            ai.setSummary(summary);
            ai.setKeyPoints(keyPoints);
            ai.setSuggestedTags(suggestedTags);
            ai.setFullResponse(fullResponse);
            aiAnalysisMapper.insert(ai);
            return ai;
        }
    }

    @Override
    @Transactional
    public Result updatePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        // 1. 验证新密码和确认密码
        if (!newPassword.equals(confirmPassword)) {
            return Result.error("两次输入的新密码不一致");
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 验证原密码
        if (!PasswordUtil.match(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 4. 更新新密码
        user.setPassword(PasswordUtil.encode(newPassword));
        userMapper.updateById(user);

        return Result.success("密码修改成功");
    }

    @Override
    public List<Map<String, Object>> getFriendNotes(Long currentUserId, Long friendId) {
        // 1. 查询分享给当前用户的笔记权限（permission_type 为 1 或 2）
        LambdaQueryWrapper<NoteSharePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteSharePermission::getUserId, currentUserId)
                .in(NoteSharePermission::getPermissionType, Arrays.asList(1, 2));

        List<NoteSharePermission> permissions = noteSharePermissionMapper.selectList(wrapper);

        if (permissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取笔记ID列表
        List<Long> noteIds = permissions.stream()
                .map(NoteSharePermission::getNoteId)
                .collect(Collectors.toList());

        // 3. 查询笔记，筛选出作者是 friendId 的
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getId, noteIds)
                .eq(Note::getUserId, friendId)   // 👈 笔记作者是好友
                .eq(Note::getIsDeleted, 0);

        List<Note> notes = noteMapper.selectList(noteWrapper);

        // 4. 组装返回数据（只返回 id、标题、权限类型）
        List<Map<String, Object>> result = new ArrayList<>();
        for (Note note : notes) {
            Integer permissionType = permissions.stream()
                    .filter(p -> p.getNoteId().equals(note.getId()))
                    .findFirst()
                    .map(NoteSharePermission::getPermissionType)
                    .orElse(1);

            Map<String, Object> item = new HashMap<>();
            item.put("id", note.getId());
            item.put("title", note.getTitle());
            item.put("permissionType", permissionType);

            result.add(item);
        }

        return result;
    }
}
