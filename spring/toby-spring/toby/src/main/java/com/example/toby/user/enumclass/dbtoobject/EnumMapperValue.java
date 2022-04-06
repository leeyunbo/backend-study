package com.example.toby.user.enumclass.dbtoobject;

public class EnumMapperValue {

    private String title;
    private String code;

    public EnumMapperValue(EnumMapperType enumMapperType) {
        code = enumMapperType.getCode();
        title = enumMapperType.getTitle();
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "{" +
                "title='" + title + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
