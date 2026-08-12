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
import com.shuzijun.leetcode.plugin.editor.LCVPreview;
import com.shuzijun.leetcode.plugin.editor.QuestionPreviewRenderMode;
import com.shuzijun.leetcode.plugin.editor.SplitFileEditor;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.listener.QuestionSubmitNotifier;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.manager.SubmissionManager;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.leetcode.plugin.utils.AsyncTaskHandle;
import com.shuzijun.leetcode.plugin.utils.AsyncUiUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.ui.ContentStatePanel;
import com.shuzijun.leetcode.plugin.window.dialog.SubmissionsPanel;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.openapi.actionSystem.ActionPlaces.TEXT_EDITOR_WITH_PREVIEW;

/**
 * @author shuzijun
 */
public class SubmissionsPreview extends UserDataHolderBase implements FileEditor {

    private static final String MY_PROPORTION_KEY = PluginConstant.PLUGIN_ID + "SubmissionsSplitEditor.Proportion";
    private static final String LIST_OPERATION = "submission-list";
    private static final String DETAIL_OPERATION = "submission-detail";

    private final Project project;
    private final LeetcodeEditor leetcodeEditor;


    private BorderLayoutPanel myComponent;
    private Question question;
    private FileEditor fileEditor;

    private boolean isLoad = false;

    private List<Submission> submissionList;
    private JBTable table;

    private JBSplitter mySplitter;
    private ContentStatePanel listState;
    private ContentStatePanel detailState;
    private SplitFileEditor.SplitEditorLayout myLayout = SplitFileEditor.SplitEditorLayout.FIRST;
    private final AtomicInteger listRequestId = new AtomicInteger();
    private final AtomicInteger submissionRequestId = new AtomicInteger();
    private AsyncTaskHandle listTask;
    private AsyncTaskHandle detailTask;

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
            listState = new ContentStatePanel();
            detailState = new ContentStatePanel();
            mySplitter.setFirstComponent(listState);
            mySplitter.setSecondComponent(detailState);
            adjustEditorsVisibility();
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
        int requestId = listRequestId.incrementAndGet();
        cancelTask(listTask);
        cancelDetailLoad();
        ApplicationManager.getApplication().invokeLater(() -> {
            if (requestId != listRequestId.get()) {
                return;
            }
            listState.showLoading(PropertiesUtils.getInfo("ui.loading"));
            listTask = AsyncUiUtils.load(project, this, LIST_OPERATION, () -> {
                if (!LeetCodeServices.login().isLoggedIn()) {
                    return InitialData.loggedOut();
                }
                Question loadedQuestion = QuestionManager.getQuestionByTitleSlug(leetcodeEditor.getTitleSlug(), project);
                List<Submission> loadedSubmissions = loadedQuestion == null
                        ? null
                        : SubmissionManager.getSubmissionService(loadedQuestion.getTitleSlug(), project);
                return InitialData.loggedIn(loadedQuestion, loadedSubmissions);
            }, (data, error) -> {
                if (requestId != listRequestId.get()) {
                    return;
                }
                if (error != null) {
                    listState.showError(
                            PropertiesUtils.getInfo("ui.submission.failed"),
                            PropertiesUtils.getInfo("ui.retry"),
                            () -> initComponent(defaultId)
                    );
                    return;
                }
                if (!data.loggedIn) {
                    listState.showLoginRequired(
                            PropertiesUtils.getInfo("login.not"),
                            PropertiesUtils.getInfo("ui.sign.in"),
                            null
                    );
                    return;
                }
                question = data.question;
                submissionList = data.submissions;
                if (question == null) {
                    listState.showEmpty(PropertiesUtils.getInfo("ui.question.empty"), null, null);
                } else {
                    if (submissionList != null && !submissionList.isEmpty()) {
                        table = new JBTable(new SubmissionsPanel.TableModel(submissionList));
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        table.getTableHeader().setReorderingAllowed(false);
                        table.setRowSelectionAllowed(true);
                        table.setFillsViewportHeight(true);
                        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
                        listState.showContent(jbScrollPane);

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
                        listState.showEmpty(PropertiesUtils.getInfo("ui.submission.empty"), null, null);
                    }
                }
            });
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

    private void openSubmission(Submission submission) {
        if (question == null) {
            return;
        }
        String titleSlug = question.getTitleSlug();
        int requestId = submissionRequestId.incrementAndGet();
        cancelTask(detailTask);
        disposeFileEditor();
        detailState.showLoading(PropertiesUtils.getInfo("ui.loading"));
        detailTask = AsyncUiUtils.load(project, this, DETAIL_OPERATION, () -> {
            File file = SubmissionManager.openSubmission(submission, titleSlug, project, false);
            return file == null || !file.exists()
                    ? null
                    : LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        }, (vf, error) -> {
            if (requestId != submissionRequestId.get()) {
                return;
            }
            if (error != null) {
                detailState.showError(
                        PropertiesUtils.getInfo("ui.submission.detail.failed"),
                        PropertiesUtils.getInfo("ui.retry"),
                        () -> openSubmission(submission)
                );
            } else if (vf == null) {
                detailState.showEmpty(PropertiesUtils.getInfo("ui.submission.empty"), null, null);
            } else {
                FileEditor newEditor = createSubmissionPreview(project, vf);
                if (newEditor == fileEditor) {
                    return;
                }
                disposeFileEditor();
                fileEditor = newEditor;
                Disposer.register(this, fileEditor);
                BorderLayoutPanel secondComponent =
                        JBUI.Panels.simplePanel(fileEditor.getComponent());
                secondComponent.addToTop(createToolbarWrapper(fileEditor.getComponent()));
                detailState.showContent(secondComponent);
                myLayout = submissionDetailLayout();
                adjustEditorsVisibility();
            }
        });
    }

    static FileEditor createSubmissionPreview(Project project, VirtualFile file) {
        return new LCVPreview(project, file, QuestionPreviewRenderMode.SOURCE_CODE);
    }

    static Class<? extends FileEditor> submissionPreviewType() {
        return LCVPreview.class;
    }

    static SplitFileEditor.SplitEditorLayout submissionDetailLayout() {
        return SplitFileEditor.SplitEditorLayout.SECOND;
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
            } else if (submissionList != null && !submissionList.isEmpty()) {
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
        return !project.isDisposed();
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
        listRequestId.incrementAndGet();
        submissionRequestId.incrementAndGet();
        cancelTask(listTask);
        cancelTask(detailTask);
        listTask = null;
        detailTask = null;
        disposeFileEditor();
    }

    @Override
    public @Nullable VirtualFile getFile() {
        if (fileEditor != null) {
            return fileEditor.getFile();
        } else {
            return null;
        }
    }

    private void cancelDetailLoad() {
        submissionRequestId.incrementAndGet();
        cancelTask(detailTask);
        detailTask = null;
        disposeFileEditor();
    }

    private static void cancelTask(@Nullable AsyncTaskHandle task) {
        if (task != null) {
            task.cancel();
        }
    }

    private void disposeFileEditor() {
        if (fileEditor != null) {
            Disposer.dispose(fileEditor);
            fileEditor = null;
        }
    }

    private static class InitialData {
        private final boolean loggedIn;
        private final Question question;
        private final List<Submission> submissions;

        private InitialData(boolean loggedIn, Question question, List<Submission> submissions) {
            this.loggedIn = loggedIn;
            this.question = question;
            this.submissions = submissions;
        }

        private static InitialData loggedOut() {
            return new InitialData(false, null, null);
        }

        private static InitialData loggedIn(Question question, List<Submission> submissions) {
            return new InitialData(true, question, submissions);
        }
    }
}
