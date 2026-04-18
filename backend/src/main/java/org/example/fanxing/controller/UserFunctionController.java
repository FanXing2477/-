package org.example.fanxing.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.fanxing.dto.NoteUpdateDto;
import org.example.fanxing.entity.*;
import org.example.fanxing.mapper.UserMapper;
import org.example.fanxing.service.UserFunctionService;
import org.example.fanxing.service.impl.UserFunctionServiceImpl;
import org.example.fanxing.util.JwtUtil;
import org.example.fanxing.util.OssUtil;
import org.example.fanxing.vo.NoteDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserFunctionController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFunctionService userFunctionService;

    @Autowired
    private OssUtil ossUtil;

    @Value("${file.max-size}")
    private Long maxSize;

    @Value("${file.allow-types}")
    private String allowTypes;

    @Autowired
    private UserFunctionServiceImpl userFunctionServiceImpl;

    //上传头像，将图片文件上传值oss
    @PostMapping("/updateAvatar")
    public Result updateAvatar(
            @RequestHeader("token") String token,
            @RequestParam("file") MultipartFile file) {
        // 1. 校验Token
        Map<String, Object> claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (JWTVerificationException e) {
            return Result.error("token无效，请重新登录");
        }
        Long userId = Long.valueOf(claims.get("id").toString());

        // 2. 校验文件
        if (file.isEmpty()) return Result.error("请选择要上传的头像");
        if (file.getSize() > maxSize) return Result.error("头像大小不能超过10MB");

        String originalFilename = file.getOriginalFilename();
        String suffix = null;
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        if (suffix == null || !allowTypes.contains(suffix.toLowerCase())) {
            return Result.error("仅支持jpg、jpeg、png、gif格式的图片");
        }

        // 3. 生成唯一文件名
        String fileName = userId + "_" + System.currentTimeMillis() + suffix;

        // 4. 上传到OSS
        String avatarUrl;
        try {
            avatarUrl = ossUtil.uploadFile(file, fileName);
        } catch (Exception e) {
            return Result.error("头像上传失败，请稍后重试");
        }

        // 5. 更新用户头像
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);

        return Result.success("头像修改成功", avatarUrl);
    }

    //上传昵称
    @PostMapping("/updateNickname")
    public Result updateNickname(
            @RequestHeader("token") String token,
            @RequestParam String nickname
    ) {
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            User user = new User();
            user.setId(userId);
            user.setNickname(nickname);
            userMapper.updateById(user);

            return Result.success("昵称修改成功");
        } catch (JWTVerificationException e) {
            return Result.error("token无效，请重新登录");
        }
    }

    //上传座右铭
    @PostMapping("/updateMotto")
    public Result updateMotto(
            @RequestHeader("token") String token,
            @RequestParam String motto
    ) {
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            User user = new User();
            user.setId(userId);
            user.setMotto(motto);
            userMapper.updateById(user);

            return Result.success("座右铭修改成功");
        } catch (JWTVerificationException e) {
            return Result.error("token无效，请重新登录");
        }
    }
    //创建标签
    @PostMapping("/create_tag")
    public Result createTag(@RequestHeader("token") String token,
                            @RequestParam String tagName){
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());
        Tag tag = userFunctionService.createTag(userId, tagName);
        return Result.success("标签创建成功", tag);

    }
    //创建一条笔记
    @PostMapping("/create_note")
    public Result createNote(
            @RequestHeader("token") String token,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String tags
    ){
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUserId(userId);
        return userFunctionServiceImpl.createANote(note,tags);
    }

    /*按时间倒叙进行分页查询,如果前端不传则默认第一页，
    每页十条笔记，传的话，则展示具体页数和每一页的笔记条数
    */
    @GetMapping("/select_by_time")
    public Result selectByTime(
            @RequestHeader("token") String token,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size){
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Integer userId = (Integer) claims.get("id");

        Page<Note> page = userFunctionService.selectPageByTimeDesc(userId,current, size);

        return Result.success(page);


    }
    //按标签进行查询
    @GetMapping("/page_by_tags")
    public Result<Page<Note>> pageByTags(
            @RequestHeader("token") String token,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam String tagNames
    ) {
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Integer userId = (Integer) claims.get("id");

        Page<Note> page = userFunctionService.pageByTagNames(userId,current, size, tagNames);
        return Result.success(page);
    }
    //修改笔记
    @PostMapping("/update_note")
    public Result updateNote(
            @RequestHeader("token") String token,
            @RequestBody NoteUpdateDto dto
    ) {
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());

        userFunctionService.updateNote(dto, userId);
        return Result.success("修改成功");
    }
    //为笔记附上标签
    @PostMapping("/update_tag")
    public Result updateTag(@RequestHeader("token") String token,
                            @RequestBody NoteUpdateDto dto){
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());

        userFunctionService.updateTag(dto, userId);
        return Result.success("成功修改标签");
    }
    //查看笔记的权限
    @GetMapping("/see_note{noteId}")
    public Result getNote(
            @PathVariable Long noteId,
            @RequestHeader("token") String token) {

        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());

        return Result.success(userFunctionService.canSeeNote(noteId,userId));
    }
    //编辑笔记的权限
    @PostMapping("/edit_note")
    public Result<?> updateNote(
            @RequestBody NoteUpdateDto dto,
            @RequestHeader("token") String token) {

        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());

        boolean success = userFunctionService.canEditNote(dto, userId);
        return success ? Result.success("编辑成功") : Result.error("编辑失败");
    }
    // 设置笔记可见范围（公开/私有/好友可见等）
    @PostMapping("/set_visibility")
    public Result setNoteVisibility(
            @RequestHeader("token") String token,
            @RequestParam Long noteId,
            @RequestParam Integer visibility
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        userFunctionService.setNoteVisibility(noteId, userId, visibility);
        return Result.success("设置成功");
    }
    // 给某个好友添加 查看/编辑 权限
    @PostMapping("/share_to_user")
    public Result shareNoteToUser(
            @RequestHeader("token") String token,
            @RequestParam Long noteId,
            @RequestParam Long targetUserId,
            @RequestParam Integer permissionType
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        userFunctionService.shareNoteToUser(noteId, userId, targetUserId, permissionType);
        return Result.success("分享成功");
    }
    //用户的逻辑删除
    @PostMapping("/delete_user")
    public Result deleteUser(@RequestHeader("token") String token) {
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());

        userFunctionService.deleteUser(userId);
        return Result.success("用户注销成功");
    }
    //笔记的逻辑删除
    @PostMapping("/delete_note")
    public Result deleteNote(
            @RequestHeader("token") String token,
            @RequestParam Long noteId
    ) {
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.get("id").toString());

        userFunctionService.deleteNote(noteId, userId);
        return Result.success("笔记删除成功");
    }
    //添加浏览记录功能
    @GetMapping("/add_history")
    public Result addBrowseHistory(
            @RequestHeader("token") String token,
            @RequestParam Long noteId
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        userFunctionService.addHistory(userId, noteId);
        return Result.success();
    }
    //查看浏览历史记录
    @GetMapping("/see_history")
    public Result<Page<Note>> historyPage(
            @RequestHeader("token") String token,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        Page<Note> page = userFunctionService.getHistory(userId, current, size);
        return Result.success(page);
    }
    //删除单条历史记录
    @PostMapping("/delete_history")
    public Result deleteHistory(
            @RequestHeader("token") String token,
            @RequestParam Long id
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        return userFunctionService.deleteHistory(id, userId);
    }
    //根据 邮箱/手机号查找用户s
    @GetMapping("/search_friend")
    public Result searchUser(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone
    ) {
        return Result.success(userFunctionService.searchUser(email, phone));
    }
    //发送好友申请
    @PostMapping("add_request")
    public Result addFriendRequest(
            @RequestHeader("token") String token,
            @RequestParam Long toUserId
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        userFunctionService.addFriendRequest(userId, toUserId);
        return Result.success("申请已发送");
    }
    //查看我收到的好友申请
    @GetMapping("/request_list")
    public Result getRequestList(
            @RequestHeader("token") String token
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        return Result.success(userFunctionService.getMyRequests(userId));
    }
    //处理好友申请
    @PostMapping("/request_handle")
    public Result handleRequest(
            @RequestHeader("token") String token,
            @RequestParam Long requestId,
            @RequestParam Integer status
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        userFunctionService.handleRequest(userId, requestId, status);
        return Result.success("操作成功");
    }
    //查看我的好友列表（根据标签分组）
    @GetMapping("/friend_list")
    public Result getMyFriends(
            @RequestHeader("token") String token
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        return Result.success(userFunctionService.getMyFriends(userId));
    }
    //修改好友分组
    @PostMapping("/update_friend_group")
    public Result updateGroup(
            @RequestHeader("token") String token,
            @RequestParam Long friendId,
            @RequestParam String groupName
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        userFunctionService.updateGroup(userId, friendId, groupName);
        return Result.success("修改成功");
    }
    //查看笔记详情
    @GetMapping("/note_detail")
    public Result<NoteDetailVo> getNoteDetail(
            @RequestHeader("token") String token,
            @RequestParam Long noteId
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        NoteDetailVo vo = userFunctionService.getNoteDetail(noteId, userId);
        return Result.success(vo);
    }
    //ai分析接口
    @PostMapping("/ai_analyze")
    public Result<AiAnalysis> aiAnalyze(
            @RequestHeader("token") String token,
            @RequestParam Long noteId
    ) {
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        AiAnalysis ai = userFunctionService.aiAnalyze(noteId, userId);
        return Result.success(ai);
    }
    //更改密码，已登录的更改密码的业务逻辑
    @PutMapping("/update_password")
    public Result updatePassword(@RequestBody Map<String, String> params,
                                 @RequestHeader("token") String token) {

        // 从 token 获取用户 ID
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Long userId = ((Number) claims.get("id")).longValue();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");

        return userFunctionService.updatePassword(userId, oldPassword, newPassword, confirmPassword);
    }
    //获取好友分享的笔记列表
    @GetMapping("/friend_notes")
    public Result getFriendNotes(@RequestParam Long friendId, @RequestHeader("token") String token){
        Long userId = Long.valueOf(JwtUtil.parseToken(token).get("id").toString());
        List<Map<String , Object>> friendNotes = userFunctionService.getFriendNotes(userId, friendId);

        return Result.success(friendNotes);
    }
}
