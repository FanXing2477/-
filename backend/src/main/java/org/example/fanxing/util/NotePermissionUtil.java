package org.example.fanxing.util;

import org.example.fanxing.entity.Note;
import org.example.fanxing.entity.NoteSharePermission;

public class NotePermissionUtil {
    /**
     * 判断是否可以【查看】笔记
     */
    public static boolean canView(Long currentUserId,
                                  Note note,
                                  NoteSharePermission permission) {

        // 1. 自己永远可以看
        if (currentUserId.equals(note.getUserId())) {
            return true;
        }

        int visibility = note.getVisibility();

        switch (visibility) {
            case 0: // 仅自己
                return false;

            case 1: // 部分好友可见
                return permission.getPermissionType() == 1 || permission.getPermissionType() == 2;
            case 2: // 部分好友可编辑
                // 必须有分享记录，且类型是查看或编辑
                return permission != null &&
                        (permission.getPermissionType() == 1 || permission.getPermissionType() == 2);

            case 3: // 公开
                return true;

            default:
                return false;
        }
    }

    /**
     * 判断是否可以【编辑】笔记
     */
    public static boolean canEdit(Long currentUserId,
                                  Note note,
                                  NoteSharePermission permission) {

        // 自己永远可以编辑（保证你不会编辑不了）
        if (currentUserId.equals(note.getUserId())) {
            return true;
        }

        // 只有笔记权限为 2，且用户权限为 1（可编辑）
        return note.getVisibility() == 2
                && permission != null
                && permission.getPermissionType() == 2;
    }
}
