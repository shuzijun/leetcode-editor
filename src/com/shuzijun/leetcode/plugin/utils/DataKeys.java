package com.shuzijun.leetcode.plugin.utils;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: mafayun
 * Date: 2019-03-29
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class DataKeys {

    public static final DataKey<JTree> LEETCODE_PROJECTS_TREE = DataKey.create("LEETCODE_PROJECTS_TREE");

    public static final DataKey<JPanel> LEETCODE_PROJECTS_TERRFIND = DataKey.create("LEETCODE_PROJECTS_TERRFIND");

}
