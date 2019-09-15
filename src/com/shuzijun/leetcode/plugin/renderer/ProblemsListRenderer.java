package com.shuzijun.leetcode.plugin.renderer;

import com.shuzijun.leetcode.plugin.model.Question;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ProblemsListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Question question = (Question) node.getUserObject();


        if(question.getLevel()==null){

        }else if(question.getLevel()==1){
            setForeground(new Color(92,184,92));
        }else if(question.getLevel()==2){
            setForeground(new Color(240,173,78));
        }else if(question.getLevel()==3){
            setForeground(new Color(217,83,79));
        }
        return this;
    }
}
