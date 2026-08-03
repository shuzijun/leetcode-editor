package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
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
    private final JComponent[] tabComponents;
    private final boolean[] tabComponentsInitialized;
    private final VirtualFile file;


    private JComponent myComponent;
    private JTabbedPane editorTabs;

    private LeetcodeEditor leetcodeEditor;

    public ConvergePreview(@NotNull FileEditor[] fileEditors, String[] names, Project project, VirtualFile file) {
        this.project = project;
        this.fileEditors = fileEditors;
        this.names = names;
        this.tabComponents = new JComponent[names.length];
        this.tabComponentsInitialized = new boolean[names.length];
        this.file = file;

        this.leetcodeEditor = ProjectConfig.getInstance(project).getEditor(file.getPath());


        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        settingsConnection.subscribe(LoginNotifier.TOPIC, new LoginNotifier() {
            @Override
            public void login(Project project, String host) {
                if (host.equals(leetcodeEditor.getHost())) {
                    for (int i = 0; i < names.length; i++) {
                        fileEditors[i].setState(LoginState.getState(true, isSelected(i)));
                    }
                }
            }

            @Override
            public void logout(Project project, String host) {
                if (host.equals(leetcodeEditor.getHost())) {
                    for (int i = 0; i < names.length; i++) {
                        fileEditors[i].setState(LoginState.getState(false, isSelected(i)));
                    }
                }
            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        if (myComponent == null) {
            editorTabs = new JTabbedPane();
            for (int i = 0; i < fileEditors.length; i++) {
                editorTabs.addTab(names[i], new JPanel());
            }
            getTabComponent(0);
            editorTabs.addChangeListener(event -> {
                int selectedIndex = editorTabs.getSelectedIndex();
                if (selectedIndex >= 0) {
                    getTabComponent(selectedIndex);
                    fileEditors[selectedIndex].setState(TabFileEditorState.TabFileEditorLoadState);
                }
            });


            myComponent = JBUI.Panels.simplePanel(editorTabs);
        }
        return myComponent;
    }

    private boolean isSelected(int index) {
        return editorTabs != null && editorTabs.getSelectedIndex() == index;
    }

    private JComponent getTabComponent(int index) {
        if (!tabComponentsInitialized[index]) {
            tabComponents[index] = fileEditors[index].getComponent();
            tabComponentsInitialized[index] = true;
            if (editorTabs != null) {
                editorTabs.setComponentAt(index, tabComponents[index]);
            }
        }
        return tabComponents[index];
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
        if (state instanceof TabSelectFileEditorState) {
            if (editorTabs != null) {
                String name = ((TabSelectFileEditorState) state).getName();
                for (int i = 0; i < names.length; i++) {
                    if (name.equals(names[i])) {
                        getTabComponent(i);
                        fileEditors[i].setState(state);
                        editorTabs.setSelectedIndex(i);
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

    public static class TabFileEditorState implements FileEditorState {

        private boolean load = false;

        public TabFileEditorState(boolean load) {
            this.load = load;
        }

        public boolean isLoad() {
            return load;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

        public static TabFileEditorState TabFileEditorLoadState = new TabFileEditorState(true);


    }

    public static class TabSelectFileEditorState implements FileEditorState {

        private String name;

        private String childrenState;

        public TabSelectFileEditorState(String name) {
            this.name = name;
        }

        public TabSelectFileEditorState(String name, String childrenState) {
            this.name = name;
            this.childrenState = childrenState;
        }

        public String getName() {
            return name;
        }

        public String getChildrenState() {
            return childrenState;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

    }

    public static class LoginState implements FileEditorState {

        public static LoginState NoLoginNoSelect = new LoginState(false, false);
        public static LoginState NoLoginSelect = new LoginState(false, true);
        public static LoginState LoginNoSelect = new LoginState(true, false);
        public static LoginState LoginSelect = new LoginState(true, true);

        public static LoginState getState(boolean login, boolean select) {
            if (login) {
                if (select) {
                    return LoginSelect;
                } else {
                    return LoginNoSelect;
                }
            } else {
                if (select) {
                    return NoLoginSelect;
                } else {
                    return NoLoginNoSelect;
                }
            }
        }

        private boolean login;

        private boolean select;

        public LoginState(boolean login, boolean select) {
            this.login = login;
            this.select = select;
        }

        public boolean isLogin() {
            return login;
        }

        public boolean isSelect() {
            return select;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

    }

}
