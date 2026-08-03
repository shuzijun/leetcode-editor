package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;

/**
 * @author zzdcon
 */
public class OpenInWebAction extends AbstractTreeAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {

        BrowserUtils.browse(URLUtils.getLeetcodeProblems() + question.getTitleSlug());
    }
}
