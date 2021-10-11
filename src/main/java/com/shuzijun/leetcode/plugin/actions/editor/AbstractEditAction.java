package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shuzijun
 */
abstract class AbstractEditAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        VirtualFile vf = ArrayUtil.getFirstElement(FileEditorManager.getInstance(anActionEvent.getProject()).getSelectedFiles());
        if (vf == null) {
            return;
        }
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(anActionEvent.getProject()).getEditor(vf.getPath());
        if (leetcodeEditor == null) {
            return;
        }
        if (StringUtils.isBlank(leetcodeEditor.getTitleSlug())) {
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("tree.null"));
            return;
        }
        if(!URLUtils.getLeetcodeHost().equals(leetcodeEditor.getHost())){
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("tree.host"));
            return;
        }
        Question question = ViewManager.getQuestionByTitleSlug(leetcodeEditor.getTitleSlug(), null, anActionEvent.getProject());
        if (question == null) {
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("tree.null"));
            return;
        }

        actionPerformed(anActionEvent, config, question);


    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Config config, Question question);
}
