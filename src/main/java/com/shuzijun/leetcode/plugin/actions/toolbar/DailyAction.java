package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author gn.binarybei
 * @date 2021/9/18
 * @note
 */
public class DailyAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        Project project = anActionEvent.getProject();
        List<Question> questionCache = QuestionManager.getQuestionCache();
        if(CollectionUtils.isNotEmpty(questionCache)){
            CodeManager.openCode(questionCache.get(0), project);
        }
    }
}
