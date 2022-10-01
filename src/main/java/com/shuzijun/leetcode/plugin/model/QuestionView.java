package com.shuzijun.leetcode.plugin.model;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class QuestionView {
    private String title;
    private String questionId;
    private Integer level;
    private String status;
    private String titleSlug;
    /**
     * 页面的题目编号
     */
    private String frontendQuestionId;

    /**
     * 通过率 %
     */
    private Double acceptance = 0D;

    /**
     * 频率
     */
    private Double frequency = 0d;

    private String category;

    public QuestionView() {
    }

    public QuestionView(String title) {
        this.title = title;
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

    public Integer getLevel() {
        return level;
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
        } else if ("1".equals(difficulty) || "2".equals(difficulty) || "3".equals(difficulty)) {
            this.level = Integer.valueOf(difficulty);
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


    public String getTitleSlug() {
        return titleSlug;
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public void setAcceptance(Double acceptance) {
        if (acceptance != null && acceptance.doubleValue() > 1) {
            acceptance = acceptance / 100;
        }
        this.acceptance = acceptance;
    }

    public Double getAcceptance() {
        return acceptance;
    }

    public Double getFrequency() {
        if (frequency != null && frequency.doubleValue() > 1) {
            frequency = frequency / 100;
        }
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public String getStatusSign() {

        if ("notac".equalsIgnoreCase(status) || "TRIED".equalsIgnoreCase(status)) {
            return "?";
        } else if ("ac".equalsIgnoreCase(status)) {
            return "✔";
        } else if ("lock".equalsIgnoreCase(status)) {
            return "$";
        } else if ("day".equalsIgnoreCase(status)) {
            return "D";
        } else if (level != null) {
            return " ";
        }
        return " ";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(getStatusSign());
        if (StringUtils.isNotBlank(frontendQuestionId)) {
            sb.append("[").append(frontendQuestionId).append("]");
        }
        return sb.append(title).toString();

    }

    private static Map<Character, Integer> SORT = new HashMap<>();

    static {
        String sortStr = "剑面";
        for (int i = 0; i < sortStr.length(); i++) {
            SORT.put(sortStr.charAt(i), i);
        }
    }

    public int frontendQuestionIdCompareTo(QuestionView questionView) {
        Integer i1 = SORT.get(frontendQuestionId.charAt(0));
        Integer i2 = SORT.get(questionView.frontendQuestionId.charAt(0));
        if (i1 != null && i2 != null) {
            if (i1 != i2) {
                return i1.compareTo(i2);
            }else {
                return frontendQuestionId.compareTo(questionView.frontendQuestionId);
            }
        } else if (i1 != null) {
            return 1;
        } else if (i2 != null) {
            return -1;
        }
        if (frontendQuestionId.length() != questionView.frontendQuestionId.length()) {
            return frontendQuestionId.length() - questionView.frontendQuestionId.length();
        }

        return frontendQuestionId.compareTo(questionView.frontendQuestionId);

    }
}
