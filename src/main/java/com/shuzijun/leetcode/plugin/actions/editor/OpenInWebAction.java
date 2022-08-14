package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.Question;
import com.shuzijun.leetcode.plugin.utils.URLUtils;

/**
 * @author zzdcon
 */
public class OpenInWebAction extends AbstractEditAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        BrowserUtil.browse(URLUtils.getLeetcodeProblems() + question.getTitleSlug());
    }
}
