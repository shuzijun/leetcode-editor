package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.shuzijun.leetcode.platform.model.ConvergeFileEditorState;
import com.shuzijun.leetcode.platform.model.LeetcodeEditor;
import com.shuzijun.leetcode.platform.notifier.LoginNotifier;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.PluginTopic;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author shuzijun
 */
public class ConvergePreview extends UserDataHolderBase implements TextEditor {

    private final Project project;
    private final FileEditor[] fileEditors;
    private final String[] names;
    private final TabInfo[] tabInfos;
    private final VirtualFile file;
    private final LeetcodeEditor leetcodeEditor;
    private JComponent myComponent;
    private JBEditorTabs jbEditorTabs;

    public ConvergePreview(@NotNull FileEditor[] fileEditors, String[] names, Project project, VirtualFile file) {
        this.project = project;
        this.fileEditors = fileEditors;
        this.names = names;
        this.tabInfos = new TabInfo[names.length];
        this.file = file;

        this.leetcodeEditor = ProjectConfig.getInstance(project).getEditor(file.getPath());


        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        settingsConnection.subscribe(PluginTopic.LOGIN_TOPIC, new LoginNotifier() {
            @Override
            public void login(Project project, String host) {
                if (host.equals(leetcodeEditor.getHost())) {
                    for (int i = 0; i < names.length; i++) {
                        fileEditors[i].setState(ConvergeFileEditorState.LoginState.getState(true, tabInfos[i] == jbEditorTabs.getSelectedInfo()));
                    }
                }
            }

            @Override
            public void logout(Project project, String host) {
                if (host.equals(leetcodeEditor.getHost())) {
                    for (int i = 0; i < names.length; i++) {
                        fileEditors[i].setState(ConvergeFileEditorState.LoginState.getState(false, tabInfos[i] == jbEditorTabs.getSelectedInfo()));
                    }
                }
            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        if (myComponent == null) {
            jbEditorTabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), this);
            for (int i = 0; i < fileEditors.length; i++) {
                TabInfo tabInfo = new TabInfo(fileEditors[i].getComponent());
                tabInfo.setText(names[i]);
                tabInfos[i] = tabInfo;
                jbEditorTabs.addTab(tabInfo);
            }
            jbEditorTabs.addListener(new TabsListener() {
                @Override
                public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                    for (int i = 0; i < names.length; i++) {
                        if (newSelection.getText().equals(names[i])) {
                            fileEditors[i].setState(ConvergeFileEditorState.TabFileEditorState.TabFileEditorLoadState);
                            break;
                        }
                    }
                }
            });


            myComponent = JBUI.Panels.simplePanel(jbEditorTabs);
        }
        return myComponent;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return fileEditors[0].getPreferredFocusedComponent();
    }

    @Override
    public @NotNull String getName() {
        return PluginConstant.LEETCODE_EDITOR_TAB_VIEW;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (state instanceof ConvergeFileEditorState.TabSelectFileEditorState) {
            if (jbEditorTabs != null) {
                String name = ((ConvergeFileEditorState.TabSelectFileEditorState) state).getName();
                for (int i = 0; i < names.length; i++) {
                    if (name.equals(names[i])) {
                        fileEditors[i].setState(state);
                        jbEditorTabs.select(tabInfos[i], true);
                    }
                }
            }
        }
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
        return fileEditors[0].getCurrentLocation();
    }

    @Override
    public void dispose() {
        for (FileEditor fileEditor : fileEditors) {
            Disposer.dispose(fileEditor);
        }
    }


    @Override
    public @Nullable VirtualFile getFile() {
        return file;
    }

    @Override
    public Editor getEditor() {
        return null;
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {

    }
}
