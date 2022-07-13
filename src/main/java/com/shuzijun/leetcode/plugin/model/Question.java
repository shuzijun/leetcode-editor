package com.shuzijun.leetcode.plugin.model;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author shuzijun
 */
public class Question extends QuestionView {

    private String testCase;
    private String exampleTestcases;
    private String langSlug;
    private String nodeType = Constant.NODETYPE_DEF;

    /**
     * 题目描述
     */
    private String content;


    /**
     * 所有的代码片段
     */
    private List<CodeSnippet> codeSnippets;

    /**
     * 文章类型
     */
    private Integer articleLive;

    /**
     * 文章标识
     */
    private String articleSlug;

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


    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getExampleTestcases() {
        return exampleTestcases;
    }

    public void setExampleTestcases(String exampleTestcases) {
        this.exampleTestcases = exampleTestcases;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLangSlug(String langSlug) {
        this.langSlug = langSlug;
    }

    public String getCode() {
        if (CollectionUtils.isEmpty(codeSnippets)) {
            return "Subscribe to unlock.";
        }
        CodeTypeEnum codeType = CodeTypeEnum.getCodeTypeEnumByLangSlug(langSlug);
        for (CodeSnippet codeSnippet : codeSnippets) {
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

    public List<CodeSnippet> getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(List<CodeSnippet> codeSnippets) {
        this.codeSnippets = codeSnippets;
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

    public Integer getColumnArticles() {
        return columnArticles;
    }

    public void setColumnArticles(Integer columnArticles) {
        this.columnArticles = columnArticles;
    }

}
