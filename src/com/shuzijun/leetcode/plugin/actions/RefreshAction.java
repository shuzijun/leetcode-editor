package com.shuzijun.leetcode.plugin.actions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;

/**
 * @author shuzijun
 */
public class RefreshAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        List<Question> questionList = QuestionManager.getQuestionService();
        if (questionList == null || questionList.isEmpty()) {
            MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("response.cache"));
            questionList = QuestionManager.getQuestionCache();
            if (questionList == null || questionList.isEmpty()) {
                MessageUtils.showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
                return;
            }
        }

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);

        DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
        root.removeAllChildren();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Question("Problems"));
        DefaultMutableTreeNode difficulty = new DefaultMutableTreeNode(new Question("Difficulty"));
        DefaultMutableTreeNode tags = new DefaultMutableTreeNode(new Question("Tags"));
        DefaultMutableTreeNode explore = new DefaultMutableTreeNode(new Question("Explore", Constant.NODETYPE_EXPLORE));
        explore.add(new DefaultMutableTreeNode(new Question(Constant.NODETYPE_LOAD, Constant.NODETYPE_LOAD)));
        root.add(node);
        root.add(difficulty);
        root.add(tags);
        root.add(explore);

        for (Question q : questionList) {
            node.add(new DefaultMutableTreeNode(q));
        }

        ImmutableListMultimap<String, Question> questionMultimap = Multimaps.index(questionList.iterator(), new Function<Question, String>() {
            @Override
            public String apply(Question question) {
                if (question.getLevel() == 1) {
                    return Constant.DIFFICULTY_EASY;
                } else if (question.getLevel() == 2) {
                    return Constant.DIFFICULTY_MEDIUM;
                } else if (question.getLevel() == 3) {
                    return Constant.DIFFICULTY_HARD;
                } else {
                    return Constant.DIFFICULTY_UNKNOWN;
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
    }
}
