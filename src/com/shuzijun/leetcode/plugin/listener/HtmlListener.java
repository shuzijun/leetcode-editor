package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.manager.ExploreManager;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.FileUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/**
 * @author shuzijun
 */
public class HtmlListener  implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(HtmlListener.class);

    private DefaultMutableTreeNode node;

    private ToolWindow toolWindow;

    private Project project;

    public HtmlListener(DefaultMutableTreeNode node, ToolWindow toolWindow, Project project) {
        this.node = node;
        this.toolWindow = toolWindow;
        this.project = project;
    }

    @Override
    public void run() {

        Question question = (Question) node.getUserObject();

        if (Constant.NODETYPE_ITEM.equals(question.getNodeType())) {
            ExploreManager.getItem(question);
            if (Constant.NODETYPE_ITEM.equals(question.getNodeType())) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("request.failed"));
                return;
            } else {
                question.setNodeType(Constant.NODETYPE_DEF);
                node.setUserObject(question);
            }
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + question.getTitle() + ".md";

        File file = new File(filePath);
        if (file.exists()) {
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
        } else {
            String body = null;
            if(Constant.ITEM_TYPE_HTML.equals(question.getLangSlug())){
                body = ExploreManager.GetHtmlArticle(question);
            }else if(Constant.ITEM_TYPE_ARTICLE.equals(question.getLangSlug())){
                body = ExploreManager.GetArticle(question);
            }
            if(StringUtils.isBlank(body)){
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("request.failed"));
                return;
            }

            FileUtils.saveFile(file, body);
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
        }
    }
}
