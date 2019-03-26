package com.shuzijun.leetcode.plugin.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.manager.ExploreManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/**
 * @author shuzijun
 */
public class OpenMenuRunnable implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(OpenMenuRunnable.class);

    private DefaultMutableTreeNode node;

    private ToolWindow toolWindow;

    private Project project;

    public OpenMenuRunnable(DefaultMutableTreeNode node, ToolWindow toolWindow, Project project) {
        this.node = node;
        this.toolWindow = toolWindow;
        this.project = project;
    }

    @Override
    public void run() {

        Question question = (Question) node.getUserObject();

        String codeType = PersistentConfig.getInstance().getInitConfig().getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("config.code"));
            return;
        }
        if (Constant.NODETYPE_ITEM.equals(question.getNodeType())) {
            ExploreManager.getItem(question);
            if (StringUtils.isBlank(question.getTitleSlug())) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("response.restrict"));
                return;
            } else {
                question.setNodeType(Constant.NODETYPE_DEF);
                node.setUserObject(question);
            }
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + question.getTitle() + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {

            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
        } else {
            try {
                HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
                StringEntity entity = new StringEntity("{\"operationName\":\"questionData\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query questionData($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    questionFrontendId\\n    boundTopicId\\n    title\\n    titleSlug\\n    content\\n    translatedTitle\\n    translatedContent\\n    isPaidOnly\\n    difficulty\\n    likes\\n    dislikes\\n    isLiked\\n    similarQuestions\\n    contributors {\\n      username\\n      profileUrl\\n      avatarUrl\\n      __typename\\n    }\\n    langToValidPlayground\\n    topicTags {\\n      name\\n      slug\\n      translatedName\\n      __typename\\n    }\\n    companyTagStats\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n      __typename\\n    }\\n    stats\\n    hints\\n    solution {\\n      id\\n      canSeeDetail\\n      __typename\\n    }\\n    status\\n    sampleTestCase\\n    metaData\\n    judgerAvailable\\n    judgeType\\n    mysqlSchemas\\n    enableRunCode\\n    enableTestMode\\n    envInfo\\n    __typename\\n  }\\n}\\n\"}");
                post.setEntity(entity);
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json");
                CloseableHttpResponse response = HttpClientUtils.executePost(post);
                if (response != null && response.getStatusLine().getStatusCode() == 200) {

                    String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                    StringBuffer sb = new StringBuffer();
                    JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");

                    sb.append(CommentUtils.createComment(jsonObject.getString(URLUtils.getDescContent()), codeTypeEnum));

                    question.setTestCase(jsonObject.getString("sampleTestCase"));

                    JSONArray jsonArray = jsonObject.getJSONArray("codeSnippets");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        if (codeTypeEnum.getType().equals(object.getString("lang"))) {
                            question.setLangSlug(object.getString("langSlug"));
                            sb.append("\n\n").append(object.getString("code").replaceAll("\\n", "\n"));
                            break;
                        }
                    }

                    FileUtils.saveFile(file, sb.toString());

                    VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                    OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
                    FileEditorManager.getInstance(project).openTextEditor(descriptor, false);

                } else {
                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("response.code"));
                }
                post.abort();
            } catch (Exception e) {
                logger.error("获取代码失败", e);
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("response.code"));
                return;
            }

        }
    }
}
