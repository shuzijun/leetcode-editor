package com.shuzijun.leetcode.plugin.model;

/**
 * 常量
 *
 * @author shuzijun
 */
public class Constant {
    /**
     * 通知分组
     */
    public static final String NOTIFICATION_GROUP = "leetcode editor";

    /**
     * 题目难度
     */
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_HARD = "Hard";
    public static final String DIFFICULTY_UNKNOWN = "Unknown";

    /**
     * 树节点类型
     */
    public static final String NODETYPE_DEF = "def";
    public static final String NODETYPE_LOAD = "load...";
    public static final String NODETYPE_EXPLORE = "explore";
    public static final String NODETYPE_CATEGORY = "category";
    public static final String NODETYPE_CARD = "card";
    public static final String NODETYPE_CHAPTER = "chapter";
    public static final String NODETYPE_ITEM = "item";
    /**
     * 探索下列表内容
     */
    public static final String ITEM_TYPE_QUESTION = "question";
    public static final String ITEM_TYPE_HTML = "htmlArticle";
    public static final String ITEM_TYPE_ARTICLE = "article";

    /**
     * 类别类型
     */
    public static final String FIND_TYPE_DIFFICULTY = "Difficulty";
    public static final String FIND_TYPE_STATUS = "Status";
    public static final String FIND_TYPE_LISTS = "Lists";
    public static final String FIND_TYPE_TAGS = "Tags";

    /**
     * 状态类型
     */
    public static final String STATUS_TODO = "Todo";
    public static final String STATUS_SOLVED = "Solved";
    public static final String STATUS_ATTEMPTED = "Attempted";

    /**
     * 默认模板
     */
    public static final String CUSTOM_FILE_NAME = "[$!{question.frontendQuestionId}]${question.title}";
    public static final String CUSTOM_TEMPLATE = "${question.content}\n\n${question.code}";
}
