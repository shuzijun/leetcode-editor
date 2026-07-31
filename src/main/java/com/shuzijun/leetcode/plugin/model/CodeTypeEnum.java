package com.shuzijun.leetcode.plugin.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author shuzijun
 */
public enum CodeTypeEnum {
    JAVA("Java", "java", ".java", "//", "/**\n%s\n*/"),
    PYTHON("Python", "python", ".py", "# ","\"\"\"\n%s\n\"\"\""),
    CPP("C++", "cpp", ".cpp", "//", "/**\n%s\n*/"),
    PYTHON3("Python3", "python3", ".py", "# ","\"\"\"\n%s\n\"\"\""),
    C("C", "c", ".c", "//", "/**\n%s\n*/"),
    CSHARP("C#", "csharp", ".cs", "//", "/**\n%s\n*/"),
    JAVASCRIPT("JavaScript", "javascript", ".js", "//", "/**\n%s\n*/"),
    RUBY("Ruby", "ruby", ".rb", "#","=begin\n%s\n=end"),
    SWIFT("Swift", "swift", ".swift", "///", "/**\n%s\n*/"),
    GO("Go", "golang", ".go", "//", "/**\n%s\n*/"),
    SCALA("Scala", "scala", ".scala", "//", "/**\n%s\n*/"),
    KOTLIN("Kotlin", "kotlin", ".kt", "//", "/**\n%s\n*/"),
    RUST("Rust", "rust", ".rs", "//", "/**\n%s\n*/"),
    PHP("PHP", "php", ".php", "//", "/**\n%s\n*/"),
    TypeScript("TypeScript", "typescript", ".ts", "//", "/**\n%s\n*/"),
    Dart("Dart", "dart", ".dart", "//", "/**\n%s\n*/"),
    Racket("Racket", "racket", ".rkt", ";", "#|\n%s\n|#"),
    Erlang("Erlang", "erlang", ".erl", "%", ""),
    Elixir("Elixir", "elixir", ".ex", "#", ""),
    BASH("Bash", "bash", ".sh", "#",": '\n%s\n'"),
    MYSQL("MySQL", "mysql", ".sql", "#", "/**\n%s\n*/"),
    ORACLE("Oracle", "oraclesql", ".sql", "#", "/**\n%s\n*/"),
    MSSQLSERVER("MS SQL Server", "mssql", ".sql", "#", "/**\n%s\n*/"),
    Pandas("Pandas", "pythondata", ".py", "#", "\"\"\"\n%s\n\"\"\""),
    PostgreSQL("PostgreSQL", "postgresql", ".sql", "--", "/**\n%s\n*/"),
    ;


    private String type;
    private String langSlug;
    private String suffix;
    private String comment;
    private String multiLineComment;

    CodeTypeEnum(String type, String langSlug, String suffix, String comment, String multiLineComment) {
        this.type = type;
        this.langSlug = langSlug;
        this.suffix = suffix;
        this.comment = comment;
        this.multiLineComment = multiLineComment;
    }

    private static final Map<String, CodeTypeEnum> MAP;
    private static final Map<String, CodeTypeEnum> LANGSLUGMAP;

    static {
        Map<String, CodeTypeEnum> codeTypes = new HashMap<String, CodeTypeEnum>();
        Map<String, CodeTypeEnum> langSlugs = new HashMap<String, CodeTypeEnum>();
        for (CodeTypeEnum c : CodeTypeEnum.values()) {
            codeTypes.put(c.getType().toUpperCase(Locale.ROOT), c);
            langSlugs.put(c.langSlug.toUpperCase(Locale.ROOT), c);
        }
        MAP = Collections.unmodifiableMap(codeTypes);
        LANGSLUGMAP = Collections.unmodifiableMap(langSlugs);
    }

    public String getType() {
        return type;
    }

    public String getLangSlug() {
        return langSlug;
    }

    public String getSuffix() {
        return suffix;
    }

    public static CodeTypeEnum getCodeTypeEnum(String type) {
        return type == null ? null : MAP.get(type.toUpperCase(Locale.ROOT));
    }

    public static CodeTypeEnum getCodeTypeEnumByLangSlug(String langSlug) {
        return langSlug == null ? null : LANGSLUGMAP.get(langSlug.toUpperCase(Locale.ROOT));
    }

    public String getComment() {
        return comment;
    }

    public String getMultiLineComment() {
        return multiLineComment;
    }
}
