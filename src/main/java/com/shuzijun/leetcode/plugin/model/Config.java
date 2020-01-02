package com.shuzijun.leetcode.plugin.model;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shuzijun
 */
public class Config {

    private Integer version;

    private String id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 用户名
     */
    private String loginName;

    /**
     * 密码
     */
    @Transient
    private String password;

    /**
     * 临时文件路径
     */
    private String filePath;

    /**
     * 语言
     */
    private String codeType;

    /**
     * url
     */
    private String url;

    /**
     * 检查更新
     */
    private Boolean update = true;

    /**
     * 使用代理
     */
    private Boolean proxy = false;

    /**
     * 自定义代码生成
     */
    private Boolean customCode = false;
    /**
     * 适应英文描述
     */
    private Boolean englishContent = false;

    /**
     * 自定义文件名
     */
    private String customFileName = Constant.CUSTOM_FILE_NAME;
    /**
     * 自定义代码
     */
    private String customTemplate = Constant.CUSTOM_TEMPLATE;
    /**
     * 用户cookie
     */
    private Map<String, String> userCookie = new HashMap<>();

    /**
     * 题目颜色
     */
    private String levelColour = "#5CB85C;#F0AD4E;#D9534F";

    private List<String> favoriteList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getProxy() {
        return proxy;
    }

    public void setProxy(Boolean proxy) {
        this.proxy = proxy;
    }

    public Boolean getCustomCode() {
        return customCode;
    }

    public void setCustomCode(Boolean customCode) {
        this.customCode = customCode;
    }

    public String getCustomFileName() {
        if (!customCode) {
            return Constant.CUSTOM_FILE_NAME;
        } else {
            return customFileName;
        }
    }

    public void setCustomFileName(String customFileName) {
        this.customFileName = customFileName;
    }

    public String getCustomTemplate() {
        if (!customCode) {
            return Constant.CUSTOM_TEMPLATE;
        } else {
            return customTemplate;
        }
    }

    public void setCustomTemplate(String customTemplate) {
        this.customTemplate = customTemplate;
    }

    public Map<String, String> getUserCookie() {
        return userCookie;
    }

    public void setUserCookie(Map<String, String> userCookie) {
        this.userCookie = userCookie;
    }

    public List<String> getFavoriteList() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            favoriteList = new ArrayList<>();
            favoriteList.add("Favorite");
        }
        return favoriteList;
    }

    public void setFavoriteList(List<String> favoriteList) {
        this.favoriteList = favoriteList;
    }

    public String getAlias() {
        if ("leetcode.com".equals(getUrl())) {
            return "en";
        } else {
            return "cn";
        }
    }

    public void addCookie(String user, String cookie) {
        userCookie.put(user, cookie);
    }

    public String getCookie(String user) {
        if (userCookie == null) {
            return null;
        }
        return userCookie.get(user);
    }

    public String getLevelColour() {
        return levelColour;
    }

    public void setLevelColour(String levelColour) {
        this.levelColour = levelColour;
    }

    public Boolean getEnglishContent() {
        return englishContent;
    }

    public void setEnglishContent(Boolean englishContent) {
        this.englishContent = englishContent;
    }
}
