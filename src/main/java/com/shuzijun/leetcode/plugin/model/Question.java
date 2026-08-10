package com.shuzijun.leetcode.plugin.model;

import com.shuzijun.lc.model.CodeSnippet;

/**
 * @author shuzijun
 */
public class Question extends com.shuzijun.lc.model.Question {

    private String langSlug;
    private String nodeType = Constant.NODETYPE_DEF;

    /**
     * 文章类型
     */
    private Integer articleLive;

    /**
     * 文章标识
     */
    private String articleSlug;

    /**
     * 文章详情请求标识
     */
    private String articleId;

    /**
     * 专栏文章
     */
    private Integer columnArticles = 0;

    public Question() {
        super();
    }

    public Question(String title) {
        super(title);
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }


    public void setLangSlug(String langSlug) {
        this.langSlug = langSlug;
    }

    public String getCode() {
        if (getCodeSnippets() == null || getCodeSnippets().isEmpty()) {
            return "Subscribe to unlock.";
        }
        CodeTypeEnum codeType = CodeTypeEnum.getCodeTypeEnumByLangSlug(langSlug);
        for (CodeSnippet codeSnippet : getCodeSnippets()) {
            if (codeType.getLangSlug().equals(codeSnippet.getLangSlug())) {
                StringBuffer sb = new StringBuffer();
                sb.append(codeType.getComment()).append(Constant.SUBMIT_REGION_BEGIN).append("\n");
                sb.append(codeSnippet.getCode()).append("\n");
                sb.append(codeType.getComment()).append(Constant.SUBMIT_REGION_END).append("\n");
                return sb.toString();
            }
        }
        return codeType.getComment() + "There is no code of " + codeType.getType() + " type for this problem";
    }

    public Integer getArticleLive() {
        return articleLive;
    }

    public void setArticleLive(Integer articleLive) {
        this.articleLive = articleLive;
    }

    public String getArticleSlug() {
        return articleSlug;
    }

    public void setArticleSlug(String articleSlug) {
        this.articleSlug = articleSlug;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Integer getColumnArticles() {
        return columnArticles;
    }

    public void setColumnArticles(Integer columnArticles) {
        this.columnArticles = columnArticles;
    }

}
