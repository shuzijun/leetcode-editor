package com.shuzijun.leetcode.plugin.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CustomCode {

    private String langSlug;
    private String fileName;
    private String template;

    public CustomCode() {
    }

    public CustomCode(String langSlug, String fileName, String template) {
        this.langSlug = langSlug;
        this.fileName = fileName;
        this.template = template;
    }

    public String getLangSlug() {
        return langSlug;
    }

    public void setLangSlug(String langSlug) {
        this.langSlug = langSlug;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CustomCode that = (CustomCode) other;
        return new EqualsBuilder()
                .append(langSlug, that.langSlug)
                .append(fileName, that.fileName)
                .append(template, that.template)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(langSlug)
                .append(fileName)
                .append(template)
                .toHashCode();
    }
}
