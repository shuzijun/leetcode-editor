package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.model.Config;

/**
 * @author shuzijun
 */
public class ShareAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        BrowserUtil.browse("https://codetop.cc/?utm_source=leetcode_editor");
    }
}
