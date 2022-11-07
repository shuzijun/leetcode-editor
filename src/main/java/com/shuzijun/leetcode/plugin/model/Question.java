package com.shuzijun.leetcode.plugin.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author shuzijun
 */
public class Question {

  private String title;
  private String questionId;
  private String questionTypename;
  private String typeName;
  private Integer level;
  private String status;
  private String titleSlug;
  private boolean leaf = Boolean.FALSE;
  private String testCase;
  /**
   * 多组测试用例
   */
  private String exampleTestcases;
  /**
   * 函数名 用于生成测试代码
   */
  private String functionName;
  private Boolean isDesign = Boolean.FALSE;
  private String returnType;
  private List<String> paramTypes;
  private String langSlug;
  private String nodeType = Constant.NODETYPE_DEF;
  /**
   * 页面的题目编号
   */
  private String frontendQuestionId;
  /**
   * 题目描述
   */
  private String content;

  /**
   * 题目代码
   */
  private String code;

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
  /**
   * 解题成功
   */
  @Deprecated
  private Integer acs = 0;
  /**
   * 提交数
   */
  @Deprecated
  private Integer submitted = 0;

  /**
   * 通过率 %
   */
  private Double acceptance = 0D;

  /**
   * 频率
   */
  private Double frequency = 0d;

  public Question() {

  }

  public Question(String title) {
    this.title = title;
  }

  public Question(String title, String nodeType) {
    this.title = title;
    this.nodeType = nodeType;
  }

  public String getTitle() {
    return title;
  }

  public String getFormTitle() {
    StringBuffer sb = new StringBuffer();
    if (StringUtils.isNotBlank(frontendQuestionId)) {
      sb.append("[").append(frontendQuestionId).append("]");
    }
    return sb.append(title).toString();
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  public String getQuestionTypename() {
    return questionTypename;
  }

  public void setQuestionTypename(String questionTypename) {
    this.questionTypename = questionTypename;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public void setLevel(String difficulty) {
    if (difficulty == null) {
      this.level = 0;
    } else if ("easy".equalsIgnoreCase(difficulty)) {
      this.level = 1;
    } else if ("medium".equalsIgnoreCase(difficulty)) {
      this.level = 2;
    } else if ("hard".equalsIgnoreCase(difficulty)) {
      this.level = 3;
    } else {
      this.level = 0;
    }
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isLeaf() {
    return leaf;
  }

  public void setLeaf(boolean leaf) {
    this.leaf = leaf;
  }

  public String getTitleSlug() {
    return titleSlug;
  }

  public void setTitleSlug(String titleSlug) {
    this.titleSlug = titleSlug;
  }

  public String getTestCase() {
    return testCase;
  }

  public void setTestCase(String testCase) {
    this.testCase = testCase;
  }

  public String getLangSlug() {
    return langSlug;
  }

  public void setLangSlug(String langSlug) {
    this.langSlug = langSlug;
  }

  public String getNodeType() {
    return nodeType;
  }

  public void setNodeType(String nodeType) {
    this.nodeType = nodeType;
  }

  public String getFrontendQuestionId() {
    return frontendQuestionId;
  }

  public void setFrontendQuestionId(String frontendQuestionId) {
    this.frontendQuestionId = frontendQuestionId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
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

  @Deprecated
  public Integer getAcs() {
    return acs;
  }

  @Deprecated
  public void setAcs(Integer acs) {
    this.acs = acs;
  }

  @Deprecated
  public Integer getSubmitted() {
    return submitted;
  }

  @Deprecated
  public void setSubmitted(Integer submitted) {
    this.submitted = submitted;
  }

  public Double getAcceptance() {
    return acceptance;
  }

  public void setAcceptance() {
    if (this.submitted == 0) {
      this.acceptance = 0D;
    } else {
      this.acceptance = Double.parseDouble(this.acs + "") / Double.parseDouble(this.submitted + "");
    }
  }

  public void setAcceptance(Double acceptance) {
    this.acceptance = acceptance;
  }

  public Double getFrequency() {
    return frequency;
  }

  public void setFrequency(Double frequency) {
    this.frequency = frequency;
  }

  public String getStatusSign() {

    if ("notac".equalsIgnoreCase(status) || "TRIED".equalsIgnoreCase(status)) {
      return "❓";
    } else if ("ac".equalsIgnoreCase(status)) {
      return "✔";
    } else if ("lock".equalsIgnoreCase(status)) {
      // todo lock为会员题
      // 为什么会员题无法打开
      return "$";
    } else if ("day".equalsIgnoreCase(status)) {
      return "day";
    } else if (leaf && level != null) {
      return "   ";
    }
    return "   ";
  }

  public String getExampleTestcases() {
    return exampleTestcases;
  }

  public void setExampleTestcases(String exampleTestcases) {
    this.exampleTestcases = exampleTestcases;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append(getStatusSign());
    if (StringUtils.isNotBlank(frontendQuestionId) && leaf) {
      sb.append("[").append(frontendQuestionId).append("]");
    }
    return sb.append(title).toString();

  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public List<String> getParamTypes() {
    return paramTypes;
  }

  public void setParamTypes(List<String> paramTypes) {
    this.paramTypes = paramTypes;
  }

  public Boolean isDesign() {
    return isDesign;
  }

  public void setDesign(Boolean design) {
    isDesign = design;
  }

}
