package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.VelocityUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/**
 * @author shuzijun
 */
public class ClearOneAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();

        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        MessageUtils.showInfoMsg(question.getFormTitle(), PropertiesUtils.getInfo("clear.success"));

    }
}
