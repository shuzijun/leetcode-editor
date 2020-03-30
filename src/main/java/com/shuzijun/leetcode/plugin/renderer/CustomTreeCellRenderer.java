package com.shuzijun.leetcode.plugin.renderer;


import com.intellij.ide.util.treeView.NodeRenderer;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author shuzijun
 */
public class CustomTreeCellRenderer extends NodeRenderer {

    private static Color Level1 = new Color(92, 184, 92);
    private static Color Level2 = new Color(240, 173, 78);
    private static Color Level3 = new Color(217, 83, 79);

    public CustomTreeCellRenderer() {
        loaColor();
    }

    public static void loaColor(){
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            Color[] colors = config.getFormatLevelColour();
            Level1 = colors[0];
            Level2 = colors[1];
            Level3 = colors[2];
        }
    }

    public static BufferedImage getResourceBufferedImage(String filePath) {
        if (CustomTreeCellRenderer.class.getClassLoader().getResourceAsStream(filePath) != null) {
            try {
                return ImageIO.read(CustomTreeCellRenderer.class.getClassLoader().getResourceAsStream(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return com.intellij.util.ui.UIUtil.createImage(10, 10, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Question question = (Question) node.getUserObject();

        if (question.getLevel() == null) {

        } else if (question.getLevel() == 1) {
            setForeground(Level1);
        } else if (question.getLevel() == 2) {
            setForeground(Level2);
        } else if (question.getLevel() == 3) {
            setForeground(Level3);
        }
       /* if(leaf){
            setIcon(new ImageIcon(getResourceBufferedImage("image/33701.png")));
        }
        if("notac".equals(question.getStatus())){
            setIcon(new ImageIcon(getResourceBufferedImage("image/18253.png")));
        }else  if("ac".equals(question.getStatus())){
            setIcon(new ImageIcon(getResourceBufferedImage("image/18271.png")));
        }*/
    }
}