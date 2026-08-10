package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.editor.SplitFileEditor;
import com.shuzijun.leetcode.plugin.manager.ArticleManager;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.lc.model.CodeMetaData;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.Session;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.AsyncTaskHandle;
import com.shuzijun.leetcode.plugin.utils.AsyncUiUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.ui.ContentStatePanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
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
public class SolutionPreview extends UserDataHolderBase implements FileEditor {

    private static final String MY_PROPORTION_KEY = PluginConstant.PLUGIN_ID + "SolutionSplitEditor.Proportion";
    private static final String LIST_OPERATION = "solution-list";
    private static final String DETAIL_OPERATION = "solution-detail";


    private final Project project;
    private final LeetcodeEditor leetcodeEditor;


    private BorderLayoutPanel myComponent;
    private Question question;
    private FileEditor fileEditor;

    private boolean isLoad = false;

    private List<Solution> solutionList;
    private JBTable table;

    private JBSplitter mySplitter;
    private ContentStatePanel listState;
    private ContentStatePanel detailState;
    private SplitFileEditor.SplitEditorLayout myLayout = SplitFileEditor.SplitEditorLayout.FIRST;
    private final AtomicInteger listRequestId = new AtomicInteger();
    private final AtomicInteger articleRequestId = new AtomicInteger();
    private AsyncTaskHandle listTask;
    private AsyncTaskHandle detailTask;

    public SolutionPreview(Project project, LeetcodeEditor leetcodeEditor) {
        this.project = project;
        this.leetcodeEditor = leetcodeEditor;
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

    private void initComponent(String defaultSlug) {
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
                Question loadedQuestion = QuestionManager.getQuestionByTitleSlug(leetcodeEditor.getTitleSlug(), project);
                List<Solution> loadedSolutions = null;
                if (loadedQuestion != null && Constant.ARTICLE_LIVE_LIST.equals(loadedQuestion.getArticleLive())) {
                    loadedSolutions = ArticleManager.getSolutionList(loadedQuestion.getTitleSlug(), project);
                }
                return new InitialData(loadedQuestion, loadedSolutions);
            }, (data, error) -> {
                if (requestId != listRequestId.get()) {
                    return;
                }
                if (error != null) {
                    myLayout = SplitFileEditor.SplitEditorLayout.FIRST;
                    adjustEditorsVisibility();
                    listState.showError(
                            PropertiesUtils.getInfo("ui.solution.failed"),
                            PropertiesUtils.getInfo("ui.retry"),
                            () -> initComponent(defaultSlug)
                    );
                    return;
                }
                question = data.question;
                solutionList = data.solutions;
                if (question == null || Constant.ARTICLE_LIVE_NONE.equals(question.getArticleLive())) {
                    listState.showEmpty(PropertiesUtils.getInfo("ui.solution.empty"), null, null);
                } else if (Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
                    openArticle();
                    myLayout = SplitFileEditor.SplitEditorLayout.SECOND;
                    adjustEditorsVisibility();
                } else if (Constant.ARTICLE_LIVE_LIST.equals(question.getArticleLive())) {
                    if (solutionList == null || solutionList.isEmpty()) {
                        listState.showEmpty(PropertiesUtils.getInfo("ui.solution.empty"), null, null);
                    } else {
                        table = new JBTable(new TableModel(solutionList));
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        table.getTableHeader().setReorderingAllowed(false);
                        table.setRowSelectionAllowed(true);
                        table.setFillsViewportHeight(true);
                        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                        table.setRowSelectionInterval(0, 0);
                        table.getColumnModel().getColumn(0).setPreferredWidth(350);
                        table.getColumnModel().getColumn(1).setPreferredWidth(200);

                        table.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                                    int row = table.getSelectedRow();
                                    openSelectedQuestion(solutionList, row);
                                }
                            }
                        });
                        table.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                                    int row = table.getSelectedRow();
                                    openSelectedQuestion(solutionList, row);
                                }

                            }
                        });
                        JBScrollPane jbScrollPane = new JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        listState.showContent(jbScrollPane);
                        if (StringUtils.isNotBlank(defaultSlug)) {
                            for (int i = 0; i < solutionList.size(); i++) {
                                if (solutionList.get(i).getSlug().equals(defaultSlug)) {
                                    openSelectedQuestion(solutionList, i);
                                    table.setRowSelectionInterval(i, i);
                                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                                }
                            }
                        }
                    }
                } else {
                    listState.showEmpty(PropertiesUtils.getInfo("ui.solution.empty"), null, null);
                }
            });
        });
    }

    private void openSelectedQuestion(List<Solution> solutionList, int row) {
        if (row < 0 || solutionList == null || row >= solutionList.size()) {
            return;
        }
        Solution solution = solutionList.get(row);
        question.setArticleSlug(solution.getSlug());
        question.setArticleId(StringUtils.defaultIfBlank(
                solution.getTopicId(),
                solution.getSlug()
        ));
        try {
            openArticle();
        } catch (Exception e) {
        }
    }

    private void openArticle() {
        if (question == null) {
            return;
        }
        String titleSlug = question.getTitleSlug();
        String articleSlug = question.getArticleSlug();
        String articleId = StringUtils.defaultIfBlank(question.getArticleId(), articleSlug);
        int requestId = articleRequestId.incrementAndGet();
        cancelTask(detailTask);
        disposeFileEditor();
        detailState.showLoading(PropertiesUtils.getInfo("ui.loading"));
        detailTask = AsyncUiUtils.load(project, this, DETAIL_OPERATION, () -> {
            File file = ArticleManager.openArticle(
                    titleSlug,
                    articleSlug,
                    articleId,
                    project,
                    false
            );
            return file == null || !file.exists()
                    ? null
                    : LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        }, (vf, error) -> {
            if (requestId != articleRequestId.get()) {
                return;
            }
            if (error != null) {
                detailState.showError(
                        PropertiesUtils.getInfo("ui.solution.detail.failed"),
                        PropertiesUtils.getInfo("ui.retry"),
                        this::openArticle
                );
            } else if (vf == null) {
                detailState.showEmpty(PropertiesUtils.getInfo("ui.solution.empty"), null, null);
            } else {
            List<FileEditorProvider> editorProviders = FileEditorProviderManager.getInstance().getProviderList(project, vf);
            if (editorProviders.isEmpty()) {
                detailState.showError(
                        PropertiesUtils.getInfo("ui.solution.detail.failed"),
                        PropertiesUtils.getInfo("ui.retry"),
                        this::openArticle
                );
                return;
            }
            FileEditor newEditor = editorProviders.get(0).createEditor(project, vf);
            if (newEditor == fileEditor) {
                return;
            }
            disposeFileEditor();
            fileEditor = newEditor;
            Disposer.register(this, fileEditor);
            BorderLayoutPanel secondComponent = JBUI.Panels.simplePanel(fileEditor.getComponent());
            if (!Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
                secondComponent.addToTop(createToolbarWrapper(fileEditor.getComponent()));
            }
            detailState.showContent(secondComponent);
            myLayout = solutionDetailLayout();
            adjustEditorsVisibility();
            }
        });
    }

    static SplitFileEditor.SplitEditorLayout solutionDetailLayout() {
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
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Solution" + TEXT_EDITOR_WITH_PREVIEW, actionGroup, true);
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
            } else if (myLayout == SplitFileEditor.SplitEditorLayout.SECOND || myLayout == SplitFileEditor.SplitEditorLayout.SPLIT) {
                try {
                    openArticle();
                } catch (Exception ignore) {
                }
            }
        } else if (state instanceof ConvergePreview.TabSelectFileEditorState) {
            String slug = ((ConvergePreview.TabSelectFileEditorState) state).getChildrenState();
            if (!isLoad) {
                initComponent(slug);
            } else if (solutionList != null && !solutionList.isEmpty()) {
                for (int i = 0; i < solutionList.size(); i++) {
                    if (solutionList.get(i).getSlug().equals(slug)) {
                        openSelectedQuestion(solutionList, i);
                        table.setRowSelectionInterval(i, i);
                        break;
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
        articleRequestId.incrementAndGet();
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
        articleRequestId.incrementAndGet();
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


    private static class TableModel extends AbstractTableModel {

        String[] columnNames = {"Title", "Tags"};

        String[][] data;

        public TableModel(List<Solution> solutionList) {
            data = new String[solutionList.size()][columnNames.length];
            for (int i = 0, j = solutionList.size(); i < j; i++) {
                Solution s = solutionList.get(i);
                data[i][0] = s.getTitle();
                data[i][1] = s.getTags();
            }
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
    }

    private static class InitialData {
        private final Question question;
        private final List<Solution> solutions;

        private InitialData(Question question, List<Solution> solutions) {
            this.question = question;
            this.solutions = solutions;
        }
    }
}
