package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class DataKeys {

    public static final DataKey<JTree> LEETCODE_PROJECTS_TREE = DataKey.create("LEETCODE_PROJECTS_TREE");
    public static final DataKey<JBScrollPane> LEETCODE_PROJECTS_SCROLL = DataKey.create("LEETCODE_PROJECTS_SCROLL");
    public static final DataKey<JPanel> LEETCODE_PROJECTS_TERRFIND = DataKey.create("LEETCODE_PROJECTS_TERRFIND");

}
