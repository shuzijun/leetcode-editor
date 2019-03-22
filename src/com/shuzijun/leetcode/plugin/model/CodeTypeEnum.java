package com.shuzijun.leetcode.plugin.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public enum CodeTypeEnum {
    JAVA("Java", ".java", "//"),
    PYTHON("Python", ".py", "#"),
    CPP("C++", ".cpp", "//"),
    PYTHON3("Python3", ".py", "#"),
    C("C", ".c", "//"),
    CSHARP("C#", ".cs", "//"),
    JAVASCRIPT("JavaScript", ".js", "//"),
    RUBY("Ruby", ".rb", "#"),
    SWIFT("Swift", ".swift", "///"),
    GO("Go", ".go", "//"),
    SCALA("Scala", ".scala", "//"),
    KOTLIN("Kotlin", ".kt", "//"),
    RUST("Rust", ".rs", "//"),
    PHP("PHP", ".php", "//"),
    ;


    private String type;
    private String suffix;
    private String comment;

    CodeTypeEnum(String type, String suffix, String comment) {
        this.type = type;
        this.suffix = suffix;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }
}
