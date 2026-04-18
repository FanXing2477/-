package org.example.fanxing.enumEntity;

public enum NoteVisibilityEnum {
    PRIVATE(0, "仅自己可见"),
    FRIEND_VIEW(1, "仅部分好友可见，不可编辑"),
    FRIEND_EDIT(2, "仅部分好友可编辑"),
    PUBLIC_VIEW(3, "所有人可见，不可编辑");

    private final int value;
    private final String desc;

    NoteVisibilityEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public int getValue() { return value; }
}
