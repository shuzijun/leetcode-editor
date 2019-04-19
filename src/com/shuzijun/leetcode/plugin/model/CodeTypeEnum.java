package com.shuzijun.leetcode.plugin.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public enum CodeTypeEnum {
    JAVA("Java", "java", ".java", "//"),
    PYTHON("Python", "python", ".py", "#"),
    CPP("C++", "cpp", ".cpp", "//"),
    PYTHON3("Python3", "python3", ".py", "#"),
    C("C", "c", ".c", "//"),
    CSHARP("C#", "csharp", ".cs", "//"),
    JAVASCRIPT("JavaScript", "javascript", ".js", "//"),
    RUBY("Ruby", "ruby", ".rb", "#"),
    SWIFT("Swift", "swift", ".swift", "///"),
    GO("Go", "golang", ".go", "//"),
    SCALA("Scala", "scala", ".scala", "//"),
    KOTLIN("Kotlin", "kotlin", ".kt", "//"),
    RUST("Rust", "rust", ".rs", "//"),
    PHP("PHP", "php", ".php", "//"),
    ;


    private String type;
    private String langSlug;
    private String suffix;
    private String comment;

    CodeTypeEnum(String type, String langSlug, String suffix, String comment) {
        this.type = type;
        this.langSlug = langSlug;
        this.suffix = suffix;
        this.comment = comment;
    }

    private static Map<String, CodeTypeEnum> MAP = new HashMap<String, CodeTypeEnum>();
    private static Map<String, CodeTypeEnum> LANGSLUGMAP = new HashMap<String, CodeTypeEnum>();

    static {
        for (CodeTypeEnum c : CodeTypeEnum.values()) {
            MAP.put(c.getType().toUpperCase(), c);
            LANGSLUGMAP.put(c.langSlug.toUpperCase(), c);
        }
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }

    public static CodeTypeEnum getCodeTypeEnum(String type) {
        return MAP.get(type.toUpperCase());
    }

    public static CodeTypeEnum getCodeTypeEnumByLangSlug(String langSlug) {
        return LANGSLUGMAP.get(langSlug.toUpperCase());
    }

    public String getComment() {
        return comment;
    }
}
