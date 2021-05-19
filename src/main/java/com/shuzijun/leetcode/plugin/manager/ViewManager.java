package com.shuzijun.leetcode.plugin.manager;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author shuzijun
 */
public class ViewManager {

    private static Map<String, Question> question = Maps.newLinkedHashMap();

    private static Map<String, List<Tag>> filter = Maps.newLinkedHashMap();

    private static boolean intersection = Boolean.FALSE;

    public static void loadServiceData(JTree tree, Project project) {
        loadServiceData(tree, project, URLUtils.getLeetcodeAll());
    }

    public static void loadServiceData(JTree tree, Project project, String url) {
        List<Question> questionList = QuestionManager.getQuestionService(project, url);
        if (questionList == null || questionList.isEmpty()) {
            MessageUtils.getInstance(project).showWarnMsg("warning", PropertiesUtils.getInfo("response.cache"));
            questionList = QuestionManager.getQuestionCache();
            if (questionList == null || questionList.isEmpty()) {
                MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
                return;
            }
        }

        question = Maps.uniqueIndex(questionList.iterator(), new Function<Question, String>() {
            @Override
            public String apply(Question question) {
                return question.getQuestionId();
            }
        });

        filter.put(Constant.FIND_TYPE_DIFFICULTY, QuestionManager.getDifficulty());
        filter.put(Constant.FIND_TYPE_STATUS, QuestionManager.getStatus());
        filter.put(Constant.FIND_TYPE_LISTS, QuestionManager.getLists());
        filter.put(Constant.FIND_TYPE_TAGS, QuestionManager.getTags());
        filter.put(Constant.FIND_TYPE_CATEGORY, QuestionManager.getCategory(url));


        DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
        root.removeAllChildren();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Question(String.format("Problems(%d)", questionList.size())));
        root.add(node);
        for (Question q : questionList) {
            node.add(new DefaultMutableTreeNode(q));
        }
        for (String key : filter.keySet()) {
            if (Constant.FIND_TYPE_CATEGORY.equals(key)) {
                continue;
            }
            DefaultMutableTreeNode filterNode = new DefaultMutableTreeNode(new Question(key));
            root.add(filterNode);
            addChild(filterNode, filter.get(key), question);
        }

        DefaultMutableTreeNode explore = new DefaultMutableTreeNode(new Question("Explore", Constant.NODETYPE_EXPLORE));
        explore.add(new DefaultMutableTreeNode(new Question(Constant.NODETYPE_LOAD, Constant.NODETYPE_LOAD)));
        root.add(explore);

        tree.updateUI();
        treeMode.reload();
    }

    public static List<Tag> getFilter(String key) {
        return filter.get(key);
    }

    public static boolean clearFilter() {
        boolean isLoad = false;
        for (String key : filter.keySet()) {
            List<Tag> tagList = filter.get(key);
            for (Tag tag : tagList) {
                if (tag.isSelect() && Constant.FIND_TYPE_CATEGORY.equals(key)) {
                    isLoad = true;
                }
                tag.setSelect(Boolean.FALSE);
            }
        }
        return isLoad;
    }

    public static void updateStatus() {
        filter.put(Constant.FIND_TYPE_STATUS, QuestionManager.getStatus());
    }

    public static boolean isIntersection() {
        return intersection;
    }

    public static void setIntersection(boolean intersection) {
        ViewManager.intersection = intersection;
    }

    public static void update(JTree tree) {
        TreeSet<String> selectQuestionList = null;
        for (String key : filter.keySet()) {
            if (Constant.FIND_TYPE_CATEGORY.equals(key)) {
                continue;
            }
            List<Tag> tagList = filter.get(key);
            TreeSet<String> tagQuestionList = null;
            for (Tag tag : tagList) {
                if (tag.isSelect()) {
                    TreeSet<String> temp = tag.getQuestions();
                    if (tagQuestionList == null) {
                        tagQuestionList = new TreeSet(new Comparator<String>() {
                            @Override
                            public int compare(String arg0, String arg1) {
                                return Integer.valueOf(arg0).compareTo(Integer.valueOf(arg1));
                            }
                        });
                        tagQuestionList.addAll(temp);
                    } else {
                        if (intersection) {
                            tagQuestionList.retainAll(temp);
                        } else {
                            tagQuestionList.addAll(temp);
                        }
                    }
                }
            }

            if (tagQuestionList != null) {
                if (selectQuestionList == null) {
                    selectQuestionList = new TreeSet(new Comparator<String>() {
                        @Override
                        public int compare(String arg0, String arg1) {
                            return Integer.valueOf(arg0).compareTo(Integer.valueOf(arg1));
                        }
                    });
                    selectQuestionList.addAll(tagQuestionList);
                } else {
                    selectQuestionList.retainAll(tagQuestionList);
                }
            }
        }

        DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
        if (root.isLeaf()) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(0);
        node.removeAllChildren();

        if (selectQuestionList == null) {
            for (Question q : question.values()) {
                node.add(new DefaultMutableTreeNode(q));
            }
            ((Question) node.getUserObject()).setTitle(String.format("Problems(%d)", node.getChildCount()));
        } else {
            for (String key : selectQuestionList) {
                Question q = question.get(key);
                if (q != null) {
                    node.add(new DefaultMutableTreeNode(q));
                }
            }
            ((Question) node.getUserObject()).setTitle(String.format("Problems(%d)", node.getChildCount()));
        }
        treeMode.reload();
        tree.expandPath(new TreePath(node.getPath()));


    }

    public static void pick(JTree tree, JBScrollPane scrollPane) {

        DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
        if (root.isLeaf()) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(0);
        if (node.isLeaf()) {
            return;
        }
        int i = (int) (Math.random() * node.getChildCount());
        DefaultMutableTreeNode select = (DefaultMutableTreeNode) node.getChildAt(i);

        TreePath toShowPath = new TreePath(select.getPath());
        tree.setSelectionPath(toShowPath);
        Rectangle bounds = tree.getPathBounds(toShowPath);
        Point point = new Point(0, (int) bounds.getY());
        JViewport viewport = scrollPane.getViewport();
        viewport.setViewPosition(point);
        return;
    }

    public static Question getTreeQuestion(JTree tree, Project project) {
        Question question = null;
        if (tree != null) {
            DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (note != null) {
                question = (Question) note.getUserObject();
                if (question != null) {
                    if ("lock".equals(question.getStatus())) {
                        question = null;
                    }
                    if (!question.isLeaf()) {
                        question = null;
                    }
                }
            }
        }
        if (question == null) {
            MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("tree.select"));
        }
        return question;
    }

    public static Question getQuestionById(String id, Project project) {
        if (question.isEmpty()) {
            MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("tree.load"));
            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    ToolWindowManager.getInstance(project).getToolWindow(WindowFactory.ID).show(null);
                }
            });
            return null;
        }
        return question.get(id);
    }

    private static void addChild(DefaultMutableTreeNode rootNode, List<Tag> Lists, Map<String, Question> questionMap) {
        if (!Lists.isEmpty()) {
            for (Tag tag : Lists) {
                long qCnt = tag.getQuestions().stream().filter(q -> questionMap.get(q) != null).count();
                Question item = new Question(String.format("%s(%d)",
                        tag.getName(), qCnt));
                Question parent = (Question) rootNode.getUserObject();
                if (parent.getTitle().equals(Constant.FIND_TYPE_TAGS)){
                    item.setNodeType(Constant.NODETYPE_TAG);
                }
                DefaultMutableTreeNode tagNode = new DefaultMutableTreeNode(item);
                rootNode.add(tagNode);
                for (String key : tag.getQuestions()) {
                    if (questionMap.get(key) != null) {
                        tagNode.add(new DefaultMutableTreeNode(questionMap.get(key)));
                    }

                }
            }

        }
    }

    public static void position(JTree tree, JBScrollPane scrollPane, Question question) {

        DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
        if (root.isLeaf()) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(0);
        if (node.isLeaf()) {
            return;
        }

        for (int i = 0, j = node.getChildCount(); i < j; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            Question nodeData = (Question) childNode.getUserObject();
            if (nodeData.getQuestionId().equals(question.getQuestionId())) {
                TreePath toShowPath = new TreePath(childNode.getPath());
                tree.setSelectionPath(toShowPath);
                Rectangle bounds = tree.getPathBounds(toShowPath);
                Point point = new Point(0, (int) bounds.getY());
                JViewport viewport = scrollPane.getViewport();
                viewport.setViewPosition(point);
                return;
            }

        }
    }

}
