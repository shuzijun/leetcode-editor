package com.shuzijun.leetcode.plugin.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.FileUtils;
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


        List<Question> questionList = QuestionManager.getQuestionService();
        if (questionList == null || questionList.isEmpty()) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "请求题目出错,将加载本地缓存");
            questionList = QuestionManager.getQuestionCache();
            if (questionList == null || questionList.isEmpty()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "加载题目失败");
                return;
            }
        }

        JViewport viewport = contentScrollPanel.getViewport();
        JTree tree = (JTree) viewport.getView();
        DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
        root.removeAllChildren();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Question("全部题目"));
        DefaultMutableTreeNode difficulty = new DefaultMutableTreeNode(new Question("难度"));
        DefaultMutableTreeNode tags = new DefaultMutableTreeNode(new Question("标签"));
        root.add(node);
        root.add(difficulty);
        root.add(tags);

        for (Question q : questionList) {
            node.add(new DefaultMutableTreeNode(q));
        }

        ImmutableListMultimap<String, Question> questionMultimap = Multimaps.index(questionList.iterator(), new Function<Question, String>() {
            @Override
            public String apply(Question question) {
                if (question.getLevel() == 1) {
                    return "简单";
                } else if (question.getLevel() == 2) {
                    return "中等";
                } else if (question.getLevel() == 3) {
                    return "困难";
                } else {
                    return "未知";
                }
            }
        });
        for (String key : questionMultimap.keySet()) {
            DefaultMutableTreeNode d = new DefaultMutableTreeNode(new Question(key));
            difficulty.add(d);
            for (Question q : questionMultimap.get(key)) {
                d.add(new DefaultMutableTreeNode(q));
            }
        }

        List<Tag> tagList = QuestionManager.getTags();
        if (!tagList.isEmpty()) {
            ImmutableMap<Integer, Question> questionImmutableMap = Maps.uniqueIndex(questionList.iterator(), new Function<Question, Integer>() {
                public Integer apply(Question question) {
                    return Integer.valueOf(question.getQuestionId());
                }
            });
            for (Tag tag : tagList) {
                DefaultMutableTreeNode tagNode = new DefaultMutableTreeNode(new Question(tag.getName()));
                tags.add(tagNode);
                for (Integer key : tag.getQuestions()) {
                    if (questionImmutableMap.get(key) != null) {
                        tagNode.add(new DefaultMutableTreeNode(questionImmutableMap.get(key)));
                    }

                }
            }

        }

        tree.updateUI();
        treeMode.reload();
        contentScrollPanel.updateUI();
    }

}

