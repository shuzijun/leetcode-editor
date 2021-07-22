package com.shuzijun.leetcode.plugin.model;

import com.intellij.util.xmlb.annotations.Transient;

import java.awt.*;
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
    private String levelColour = Constant.LEVEL_COLOUR;

    /**
     * 使用jcef渲染
     */
    private Boolean jcef = false;

    /**
     * question Split Editor
     */
    private Boolean questionEditor = true;

    /**
     * Content  Multiline Comment
     */
    private Boolean multilineComment = false;

    /**
     * html Content
     */
    private Boolean htmlContent = false;

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
    @Transient
    public Color[] getFormatLevelColour() {
        Color[] formatColors = new Color[3];
        formatColors[0] = new Color(92, 184, 92);
        formatColors[1] = new Color(240, 173, 78);
        formatColors[2] = new Color(217, 83, 79);
        String[] colors = getLevelColour().split(";");
        if (colors.length > 0) {
            try {
                formatColors[0] = new Color(Integer.parseInt(colors[0].replace("#", ""), 16));
            } catch (Exception ignore) {
            }
        }
        if (colors.length > 1) {
            try {
                formatColors[1] = new Color(Integer.parseInt(colors[1].replace("#", ""), 16));
            } catch (Exception ignore) {
            }
        }
        if (colors.length > 2) {
            try {
                formatColors[2] = new Color(Integer.parseInt(colors[2].replace("#", ""), 16));
            } catch (Exception ignore) {
            }
        }
        return formatColors;
    }

    public void setLevelColour(String levelColour) {
        if(levelColour ==null || levelColour.isEmpty()){
            this.levelColour = Constant.LEVEL_COLOUR;
        }else {
            this.levelColour = levelColour;
        }
    }

    @Transient
    public void setFormatLevelColour(Color... colors) {
        String levelColour = "";
        if (colors != null && colors.length > 0) {
            for (Color color : colors) {
                String R = Integer.toHexString(color.getRed());
                R = R.length() < 2 ? ('0' + R) : R;
                String G = Integer.toHexString(color.getGreen());
                G = G.length() < 2 ? ('0' + G) : G;
                String B = Integer.toHexString(color.getBlue());
                B = B.length() < 2 ? ('0' + B) : B;

                levelColour = levelColour + '#' + R + G + B + ";";
            }
        }
        this.levelColour = levelColour;
    }

    public Boolean getEnglishContent() {
        return englishContent;
    }

    public void setEnglishContent(Boolean englishContent) {
        this.englishContent = englishContent;
    }

    public Boolean getJcef() {
        return jcef;
    }

    public void setJcef(Boolean jcef) {
        this.jcef = jcef;
    }

    public Boolean getQuestionEditor() {
        return questionEditor;
    }

    public void setQuestionEditor(Boolean questionEditor) {
        this.questionEditor = questionEditor;
    }

    public Boolean getMultilineComment() {
        return multilineComment;
    }

    public void setMultilineComment(Boolean multilineComment) {
        this.multilineComment = multilineComment;
    }

    public Boolean getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(Boolean htmlContent) {
        this.htmlContent = htmlContent;
    }

    public boolean isModified(Config config){
        if(config ==null){
            return false;
        }
        if (version != null ? !version.equals(config.version) : config.version != null) return false;
        if (loginName != null ? !loginName.equals(config.loginName) : config.loginName != null) return false;
        if (filePath != null ? !filePath.equals(config.filePath) : config.filePath != null) return false;
        if (codeType != null ? !codeType.equals(config.codeType) : config.codeType != null) return false;
        if (url != null ? !url.equals(config.url) : config.url != null) return false;
        if (update != null ? !update.equals(config.update) : config.update != null) return false;
        if (proxy != null ? !proxy.equals(config.proxy) : config.proxy != null) return false;
        if (customCode != null ? !customCode.equals(config.customCode) : config.customCode != null) return false;
        if (englishContent != null ? !englishContent.equals(config.englishContent) : config.englishContent != null)
            return false;
        if (customFileName != null ? !customFileName.equals(config.customFileName) : config.customFileName != null)
            return false;
        if (customTemplate != null ? !customTemplate.equals(config.customTemplate) : config.customTemplate != null)
            return false;
        if (jcef != null ? !jcef.equals(config.jcef) : config.jcef != null)
            return false;
        if (questionEditor != null ? !questionEditor.equals(config.questionEditor) : config.questionEditor != null)
            return false;
        if (multilineComment != null ? !multilineComment.equals(config.multilineComment) : config.multilineComment != null)
            return false;
        if (htmlContent != null ? !htmlContent.equals(config.htmlContent) : config.htmlContent != null)
            return false;
        return levelColour != null ? levelColour.equals(config.levelColour) : config.levelColour == null;
    }


}
