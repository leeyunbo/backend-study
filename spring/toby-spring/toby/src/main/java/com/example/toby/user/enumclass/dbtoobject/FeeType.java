package com.example.toby.user.enumclass.dbtoobject;

public enum FeeType implements EnumMapperType {
    PERCENT("정율"),
    MONEY("정액");

    private String title;

    FeeType(String title) {
        this.title = title;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
