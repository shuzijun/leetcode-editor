package com.shuzijun.leetcode.plugin.editor;

import com.google.common.net.UrlEscapers;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.io.URLUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author shuzijun
 */
public class LCVPreview extends UserDataHolderBase implements FileEditor {

    private final Project myProject;
    private final VirtualFile myFile;
    private final Document myDocument;

    private BorderLayoutPanel myHtmlPanelWrapper;
    private LCVPanel myPanel;

    private boolean isPresentableUrl;

    public LCVPreview(@NotNull Project project, @NotNull VirtualFile file) {
        myProject = project;
        myFile = file;
        myDocument = FileDocumentManager.getInstance().getDocument(myFile);
        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        settingsConnection.subscribe(EditorColorsManager.TOPIC, scheme -> {
            if (myPanel != null) {
                myPanel.updateStyle();
            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        if (myHtmlPanelWrapper == null) {
            myHtmlPanelWrapper = JBUI.Panels.simplePanel();
            isPresentableUrl = myProject.getPresentableUrl() != null;

            JBLabel loadingLabel = new JBLabel("Loading......");
            myHtmlPanelWrapper.addToCenter(loadingLabel);
            String url = UrlEscapers.urlFragmentEscaper().escape(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR + FileUtils.separator() + myFile.getPath());
            LCVPanel tempPanel = null;
            try {
                try {
                    tempPanel = new LCVPanel(url, myProject, myDocument.getText());
                } catch (IllegalArgumentException e) {
                    tempPanel = new LCVPanel(url, myProject, myDocument.getText(), true);
                }
                myHtmlPanelWrapper.addToCenter(tempPanel.getComponent());

            } catch (Throwable e) {
                myHtmlPanelWrapper.addToCenter(new JBLabel("<html><body>Your environment does not support JCEF.<br>Check the Registry 'ide.browser.jcef.enabled'.<br>" + e.getMessage() + "<body></html>"));
            } finally {
                myPanel = tempPanel;
                myHtmlPanelWrapper.remove(loadingLabel);
                myHtmlPanelWrapper.repaint();
            }
        }

        return myHtmlPanelWrapper;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return PluginConstant.LEETCODE_EDITOR_VIEW;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        if (myPanel != null) {
            Disposer.dispose(myPanel);
        }
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return myFile;
    }

}
