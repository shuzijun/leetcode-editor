package com.shuzijun.leetcode.plugin.actions.tree;

import com.shuzijun.leetcode.plugin.model.Question;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.List;

/**
 * @author hongjinfeng
 * @date 2021/5/20 9:41 上午
 */
public class SortByNameAction extends AbstractSortAction {

    @Override
    public void sortChildren(Question tag, List<MutableTreeNode> childrenForSort) {
        childrenForSort.sort((o1, o2) -> {
            DefaultMutableTreeNode item1 = (DefaultMutableTreeNode) o1;
            Question question1 = (Question) item1.getUserObject();
            DefaultMutableTreeNode item2 = (DefaultMutableTreeNode) o2;
            Question question2 = (Question) item2.getUserObject();
            return tag.getNameSortTrend() * (question1.getTitle().compareTo(question2.getTitle()));
        });
        tag.setNameSortTrend(-tag.getNameSortTrend());
    }
}
