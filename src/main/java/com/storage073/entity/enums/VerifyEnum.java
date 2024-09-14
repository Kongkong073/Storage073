package com.storage073.entity.enums;

public enum VerifyEnum {
    NO("", "不校验"),
    POSITIVE_INTEGER("^[0-9]*[1-9][0-9]*$", "正整数"),
    EMAIL("^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", "邮箱"),
    PHONE("^(1[0-9])\\d{9}$", "手机号"),
    PASSWORD("^.{8,18}$", "只能是数字、字母、特殊字符 8~18位"),
    ACCOUNT("^[0-9a-zA-Z]{1,}$", "字母开头，由数字、英文字母或者下划线组成");

    private String regex;
    private String desc;

    VerifyEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}
