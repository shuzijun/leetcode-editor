package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.editor.SplitFileEditor;
import com.shuzijun.leetcode.plugin.listener.QuestionSubmitNotifier;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.manager.SubmissionManager;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
import com.shuzijun.leetcode.plugin.utils.FileEditorProviderReflection;
import com.shuzijun.leetcode.plugin.window.dialog.SubmissionsPanel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import static com.intellij.openapi.actionSystem.ActionPlaces.TEXT_EDITOR_WITH_PREVIEW;

/**
 * @author shuzijun
 */
public class SubmissionsPreview extends UserDataHolderBase implements FileEditor {

    private static final String MY_PROPORTION_KEY = PluginConstant.PLUGIN_ID + "SubmissionsSplitEditor.Proportion";

    private final Project project;
    private final LeetcodeEditor leetcodeEditor;


    private BorderLayoutPanel myComponent;
    private Question question;
    private FileEditor fileEditor;

    private boolean isLoad = false;

    private List<Submission> submissionList;
    private JBTable table;

    private JBSplitter mySplitter;
    private SplitFileEditor.SplitEditorLayout myLayout = SplitFileEditor.SplitEditorLayout.FIRST;

    public SubmissionsPreview(Project project, LeetcodeEditor leetcodeEditor) {
        this.project = project;
        this.leetcodeEditor = leetcodeEditor;
        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);

        settingsConnection.subscribe(QuestionSubmitNotifier.TOPIC, new QuestionSubmitNotifier() {
            @Override
            public void submit(String host, String slug) {
                if (leetcodeEditor.getTitleSlug().equals(slug) && leetcodeEditor.getHost().equals(host)) {
                    if (isLoad) {
                        initComponent(null);
                    }
                }
            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        if (myComponent == null) {
            mySplitter = new JBSplitter(false, 0.35f, 0.15f, 0.85f);
            mySplitter.setSplitterProportionKey(MY_PROPORTION_KEY);
            mySplitter.setDividerWidth(3);
            myComponent = JBUI.Panels.simplePanel();
            myComponent.add(mySplitter, BorderLayout.CENTER);
            if (isLoad) {
                initComponent(null);
            }
        }
        return myComponent;
    }

    private void initComponent(String defaultId) {
        isLoad = true;
        ApplicationManager.getApplication().invokeLater(() -> {
            JBLabel loadingLabel = new JBLabel("Loading......");
            mySplitter.setFirstComponent(loadingLabel);
            try {
                question = QuestionManager.getQuestionByTitleSlug(leetcodeEditor.getTitleSlug(), project);

                if (question == null) {
                    mySplitter.setFirstComponent(new JBLabel("No question"));
                } else {
                    submissionList = ApplicationManager.getApplication().executeOnPooledThread(() -> SubmissionManager.getSubmissionService(question.getTitleSlug(), project)).get();
                    if (CollectionUtils.isNotEmpty(submissionList)) {
                        table = new JBTable(new SubmissionsPanel.TableModel(submissionList));
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        table.getTableHeader().setReorderingAllowed(false);
                        table.setRowSelectionAllowed(true);
                        table.setRowSelectionInterval(0, 0);
                        table.getColumnModel().getColumn(0).setPreferredWidth(150);
                        table.getColumnModel().getColumn(1).setPreferredWidth(100);
                        table.getColumnModel().getColumn(2).setPreferredWidth(50);
                        table.getColumnModel().getColumn(3).setPreferredWidth(100);
                        table.getColumnModel().getColumn(4).setPreferredWidth(50);

                        table.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                                    int row = table.getSelectedRow();
                                    openSelectedQuestion(submissionList, row);
                                }
                            }
                        });
                        table.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                                    int row = table.getSelectedRow();
                                    openSelectedQuestion(submissionList, row);
                                }

                            }
                        });
                        JBScrollPane jbScrollPane = new JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        mySplitter.setFirstComponent(jbScrollPane);

                        if (StringUtils.isNotBlank(defaultId)) {
                            for (int i = 0; i < submissionList.size(); i++) {
                                if (submissionList.get(i).getId().equals(defaultId)) {
                                    openSelectedQuestion(submissionList, i);
                                    table.setRowSelectionInterval(i, i);
                                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                                }
                            }
                        }

                    } else {
                        mySplitter.setFirstComponent(new JBLabel("No login or no submissions"));
                    }
                }
            } catch (Exception e) {
                mySplitter.setFirstComponent(new JBLabel(e.getMessage()));
            } finally {
                mySplitter.remove(loadingLabel);
            }
        });

    }

    private void openSelectedQuestion(List<Submission> submissionList, int row) {
        if (row < 0 || submissionList == null || row >= submissionList.size()) {
            return;
        }
        Submission submission = submissionList.get(row);
        try {
            openSubmission(submission);
        } catch (Exception e) {
        }
    }

    private void openSubmission(Submission submission) throws InterruptedException, java.util.concurrent.ExecutionException {
        File file = ApplicationManager.getApplication().executeOnPooledThread(() -> SubmissionManager.openSubmission(submission, question.getTitleSlug(), project, false)).get();
        if (file == null || !file.exists()) {
            mySplitter.setSecondComponent(new JBLabel("no submission"));
        } else {
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            FileEditorProvider[] editorProviders = FileEditorProviderReflection.getProviders(project, vf);
            FileEditor newEditor = editorProviders[0].createEditor(project, vf);
            if (newEditor == fileEditor) {
                return;
            }
            if (fileEditor != null) {
                mySplitter.setSecondComponent(new JBLabel("Loading......"));
                FileEditor temp = fileEditor;
                Disposer.dispose(temp);
            }
            fileEditor = newEditor;
            Disposer.register(this, fileEditor);
            BorderLayoutPanel secondComponent = JBUI.Panels.simplePanel(fileEditor.getComponent());
            secondComponent.addToTop(createToolbarWrapper(fileEditor.getComponent()));
            mySplitter.setSecondComponent(secondComponent);
        }
    }

    private SplitEditorToolbar createToolbarWrapper(JComponent targetComponentForActions) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(new AnAction("Close", "Close", AllIcons.Actions.Close) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                myLayout = SplitFileEditor.SplitEditorLayout.FIRST;
                adjustEditorsVisibility();
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Submissions" + TEXT_EDITOR_WITH_PREVIEW, actionGroup, true);
        actionToolbar.setTargetComponent(targetComponentForActions);
        return new SplitEditorToolbar(null, actionToolbar);
    }

    private void adjustEditorsVisibility() {
        if (mySplitter.getFirstComponent() != null) {
            if (myLayout == SplitFileEditor.SplitEditorLayout.FIRST || myLayout == SplitFileEditor.SplitEditorLayout.SPLIT) {
                mySplitter.getFirstComponent().setVisible(true);
            } else {
                mySplitter.getFirstComponent().setVisible(false);
            }
        }

        if (mySplitter.getSecondComponent() != null) {
            if (myLayout == SplitFileEditor.SplitEditorLayout.SECOND || myLayout == SplitFileEditor.SplitEditorLayout.SPLIT) {
                mySplitter.getSecondComponent().setVisible(true);
            } else {
                mySplitter.getSecondComponent().setVisible(false);
            }
        }
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return myComponent;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return PluginConstant.LEETCODE_EDITOR_TAB_VIEW + " Solution";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (state instanceof ConvergePreview.TabFileEditorState) {
            if (!isLoad && ((ConvergePreview.TabFileEditorState) state).isLoad()) {
                initComponent(null);
            }
        } else if (state instanceof ConvergePreview.TabSelectFileEditorState) {
            String id = ((ConvergePreview.TabSelectFileEditorState) state).getChildrenState();
            if (!isLoad) {
                initComponent(id);
            } else if (CollectionUtils.isNotEmpty(submissionList)) {
                for (int i = 0; i < submissionList.size(); i++) {
                    if (submissionList.get(i).getId().equals(id)) {
                        openSelectedQuestion(submissionList, i);
                        table.setRowSelectionInterval(i, i);
                        return;
                    }
                }
            }
        } else if (state instanceof ConvergePreview.LoginState) {
            ConvergePreview.LoginState loginState = (ConvergePreview.LoginState) state;
            if (isLoad) {
                if (loginState.isSelect()) {
                    initComponent(null);
                } else {
                    isLoad = false;
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
        return false;
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
        if (fileEditor != null) {
            Disposer.dispose(fileEditor);
        }
    }

    @Override
    public @Nullable VirtualFile getFile() {
        if (fileEditor != null) {
            return fileEditor.getFile();
        } else {
            return null;
        }
    }
}
