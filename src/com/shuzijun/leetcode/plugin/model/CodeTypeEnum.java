package com.shuzijun.leetcode.plugin.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public enum CodeTypeEnum {
    JAVA("Java", ".java", "//"),
    PYTHON("Python", ".py", "#"),;


    private String type;
    private String suffix;
    private String annotation;

    CodeTypeEnum(String type, String suffix, String annotation) {
        this.type = type;
        this.suffix = suffix;
        this.annotation = annotation;
    }

    private static Map<String, CodeTypeEnum> MAP = new HashMap<String, CodeTypeEnum>();

    static {
        for (CodeTypeEnum c : CodeTypeEnum.values()) {
            MAP.put(c.getType(), c);
        }
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }

    public static CodeTypeEnum getCodeTypeEnum(String type) {
        return MAP.get(type);
    }

    public String getAnnotation() {
        return annotation;
    }
}
