package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;

import java.io.File;

/**
 * @author shuzijun
 */
public class ClearMenuRunnable implements Runnable {

    private Question question;

    private ToolWindow toolWindow;

    public ClearMenuRunnable(Question question, ToolWindow toolWindow) {
        this.question = question;
        this.toolWindow = toolWindow;
    }

    @Override
    public void run() {
        String codeType = PersistentConfig.getInstance().getInitConfig().getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "提示", "请先配置代码类型");
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + question.getTitle() + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "提示", "清理成功");
    }
}
