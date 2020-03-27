package com.shuzijun.leetcode.plugin.listener;

import com.intellij.ui.ColorPicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class ColorListener extends MouseAdapter {
    private JLabel label;
    private JPanel mainPanel;

    public ColorListener(JPanel mainPanel,JLabel label) {
        this.mainPanel = mainPanel;
        this.label = label;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Color newColor = ColorPicker.showDialog(mainPanel, label.getText()+" Color", label.getForeground(), true, null, true);
        if(newColor!=null){
            label.setForeground(newColor);
        }
    }


}
