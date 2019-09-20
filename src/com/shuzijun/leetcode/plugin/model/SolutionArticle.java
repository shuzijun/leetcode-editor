package com.shuzijun.leetcode.plugin.model;

public class SolutionArticle {
    private String author;
    private String summary;
    private String content;
    private int upvoteCount;//点赞数

    public SolutionArticle() {
    }

    public SolutionArticle(String author, String summary, String content, int upvoteCount) {
        this.author = author;
        this.summary = summary;
        this.content = content;
        this.upvoteCount = upvoteCount;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(int upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("####Author              ").append(author).append("  \n");
        builder.append("####Votes               ").append(upvoteCount).append("  \n");
        builder.append("____________________________________________________________________________\n\n");

//        builder.append(summary).append("\n");
        builder.append(content).append("\n  ");

        return builder.toString();
    }
}
