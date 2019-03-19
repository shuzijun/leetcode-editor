package com.shuzijun.leetcode.plugin.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.HttpClientUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author shuzijun
 */
public class LoadListener implements ActionListener {

    private final static Logger logger = LoggerFactory.getLogger(LoadListener.class);

    private ToolWindow toolWindow;
    private JBScrollPane contentScrollPanel;

    public LoadListener(ToolWindow toolWindow, JBScrollPane contentScrollPanel) {
        this.toolWindow = toolWindow;
        this.contentScrollPanel = contentScrollPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String filePath = PersistentConfig.getInstance().getTempFilePath() + "all.json";
        String filePathTranslation = PersistentConfig.getInstance().getTempFilePath() + "translation.json";

        HttpGet httpget = new HttpGet(URLUtils.getLeetcodeAll());
        CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
        if (response != null && response.getStatusLine().getStatusCode() == 200) {
            try {
                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                File file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file, Boolean.FALSE);
                fileOutputStream.write(body.getBytes("UTF-8"));
                fileOutputStream.close();
            } catch (IOException e1) {
                logger.error("获取题目内容错误", e1);
            }
        } else {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "获取题目失败，将加载本地缓存");
        }
        httpget.abort();

        if (URLUtils.getQuestionTranslation()) {
            HttpPost translationPost = new HttpPost(URLUtils.getLeetcodeGraphql());
            try {
                StringEntity entityCode = new StringEntity("{\"operationName\":\"getQuestionTranslation\",\"variables\":{},\"query\":\"query getQuestionTranslation($lang: String) {\\n  translations: allAppliedQuestionTranslations(lang: $lang) {\\n    title\\n    question {\\n      questionId\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
                translationPost.setEntity(entityCode);
                translationPost.setHeader("Accept", "application/json");
                translationPost.setHeader("Content-type", "application/json");
                CloseableHttpResponse responseCode = HttpClientUtils.executePost(translationPost);
                if (responseCode != null && responseCode.getStatusLine().getStatusCode() == 200) {
                    String body = EntityUtils.toString(responseCode.getEntity(), "UTF-8");
                    File file = new File(filePathTranslation);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file, Boolean.FALSE);
                    fileOutputStream.write(body.getBytes("UTF-8"));
                    fileOutputStream.close();
                }
            } catch (IOException e1) {
                logger.error("获取题目翻译错误", e1);
            }
            translationPost.abort();
        }


        File file = new File(filePath);
        if (!file.exists()) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "加载题目失败");
        } else {

            String all = "";
            Long filelength = file.length();
            byte[] filecontent = new byte[filelength.intValue()];
            try {
                FileInputStream in = new FileInputStream(file);
                in.read(filecontent);
                in.close();
                all = new String(filecontent, "UTF-8");
            } catch (IOException i) {
                logger.error("读取文件错误", e);
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "读取题目文件错误，请尝试登陆后重新加载");
                return;
            }

            if (StringUtils.isBlank(all)) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "读取题目文件错误，请尝试登陆后重新加载");
                return;
            }

            File translationFile = new File(filePathTranslation);
            Map<String, String> translationMap = new HashMap<String, String>();
            if (URLUtils.getQuestionTranslation() && translationFile.exists()) {
                String translatio;
                Long translationFileLength = translationFile.length();
                byte[] translationFileContent = new byte[translationFileLength.intValue()];
                try {
                    FileInputStream in = new FileInputStream(translationFile);
                    in.read(translationFileContent);
                    in.close();
                    translatio = new String(translationFileContent, "UTF-8");
                    if (StringUtils.isNotBlank(translatio)) {
                        JSONArray jsonArray = JSONObject.parseObject(translatio).getJSONObject("data").getJSONArray("translations");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            translationMap.put(object.getJSONObject("question").getString("questionId"), object.getString("title"));
                        }
                    }
                } catch (IOException i) {
                    logger.error("读取翻译文件错误", e);
                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "读取翻译文件错误，将加载英文");
                    return;
                }
            }

            JViewport viewport = contentScrollPanel.getViewport();
            JTree tree = (JTree) viewport.getView();
            DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
            root.removeAllChildren();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Question("全部题目"));
            root.add(node);

            JSONArray jsonArray = JSONObject.parseObject(all).getJSONArray("stat_status_pairs");

            List<Question> questionList = new ArrayList<Question>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Question question = new Question(object.getJSONObject("stat").getString("question__title"));
                question.setLeaf(Boolean.TRUE);
                question.setQuestionId(object.getJSONObject("stat").getString("question_id"));
                try {
                    question.setStatus(object.get("status") == null ? "" : object.getString("status"));
                } catch (Exception ee) {
                    question.setStatus("");
                }
                question.setTitleSlug(object.getJSONObject("stat").getString("question__title_slug"));
                question.setLevel(object.getJSONObject("difficulty").getInteger("level"));

                if (URLUtils.getQuestionTranslation() && translationMap.containsKey(question.getQuestionId())) {
                    question.setTitle(translationMap.get(question.getQuestionId()));
                }
                questionList.add(question);
            }
            Collections.sort(questionList, new Comparator<Question>() {
                public int compare(Question arg0, Question arg1) {
                    return Integer.valueOf(arg0.getQuestionId()).compareTo(Integer.valueOf(arg1.getQuestionId()));
                }
            });

            for (Question q : questionList) {
                node.add(new DefaultMutableTreeNode(q));
            }
            tree.updateUI();
            treeMode.reload();
            contentScrollPanel.updateUI();
        }


    }
}
