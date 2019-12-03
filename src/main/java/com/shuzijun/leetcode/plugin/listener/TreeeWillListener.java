package com.shuzijun.leetcode.plugin.listener;


import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.manager.ExploreManager;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.util.List;

/**
 * @author shuzijun
 */
public class TreeeWillListener implements TreeWillExpandListener {


    private JTree tree;

    private ToolWindow toolWindow;

    public TreeeWillListener(JTree tree, ToolWindow toolWindow) {
        this.tree = tree;
        this.toolWindow = toolWindow;
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        TreePath selPath = event.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
        Question question = (Question) node.getUserObject();
        if (!isOneOpen(node)) {
            return;
        } else if ("lock".equals(question.getStatus())) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "提示", "no permissions");
            throw new ExpandVetoException(event);
        }
        if (Constant.NODETYPE_EXPLORE.equals(question.getNodeType())) {
            List<Question> category = ExploreManager.getCategory();
            if (category.isEmpty()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", PropertiesUtils.getInfo("response.type.failed", "category"));
                throw new ExpandVetoException(event);
            }
            node.removeAllChildren();
            for (Question q : category) {
                DefaultMutableTreeNode categoriesNode = new DefaultMutableTreeNode(q);
                node.add(addLoad(categoriesNode));
            }

        }
        if (Constant.NODETYPE_CATEGORY.equals(question.getNodeType())) {
            List<Question> cartList = ExploreManager.getCards(question);
            if (cartList.isEmpty()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", PropertiesUtils.getInfo("response.type.failed", "cards"));
                throw new ExpandVetoException(event);
            }
            node.removeAllChildren();
            for (Question cart : cartList) {
                node.add(addLoad(new DefaultMutableTreeNode(cart)));
            }
        } else if (Constant.NODETYPE_CARD.equals(question.getNodeType())) {
            List<Question> chapters = ExploreManager.getChapters(question);
            if (chapters.isEmpty()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", PropertiesUtils.getInfo("response.type.failed", "chapter"));
                throw new ExpandVetoException(event);
            }
            node.removeAllChildren();

            for (Question chapter : chapters) {
                node.add(addLoad(new DefaultMutableTreeNode(chapter)));
            }

        } else if (Constant.NODETYPE_CHAPTER.equals(question.getNodeType())) {
            List<Question> chapters = ExploreManager.getChapterItem(question);
            if (chapters.isEmpty()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", PropertiesUtils.getInfo("response.type.failed", "Question"));
                throw new ExpandVetoException(event);
            }
            node.removeAllChildren();
            for (Question chapter : chapters) {
                node.add(new DefaultMutableTreeNode(chapter));
            }
        }


        //throw new ExpandVetoException(event);
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

    }

    private boolean isOneOpen(DefaultMutableTreeNode node) {
        if (node.getChildCount() == 1) {
            Question question = (Question) ((DefaultMutableTreeNode) node.getChildAt(0)).getUserObject();
            if (Constant.NODETYPE_LOAD.equals(question.getNodeType())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private DefaultMutableTreeNode addLoad(DefaultMutableTreeNode node) {
        Question question = (Question) node.getUserObject();
        if ("lock".equals(question.getStatus())) {
            return node;
        }
        node.add(new DefaultMutableTreeNode(new Question(Constant.NODETYPE_LOAD, Constant.NODETYPE_LOAD)));
        return node;
    }
}
