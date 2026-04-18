package org.example.fanxing.enumEntity;

public enum SharePermissionEnum {
    VIEW(0, "可查看"),
    EDIT(1, "可编辑");

    private final int value;
    private final String desc;

    SharePermissionEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public int getValue() { return value; }
}
