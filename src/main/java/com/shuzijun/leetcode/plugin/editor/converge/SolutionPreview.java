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
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.editor.SplitFileEditor;
import com.shuzijun.leetcode.plugin.manager.ArticleManager;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.FileEditorProviderReflection;
import org.apache.commons.collections.CollectionUtils;
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

import static com.intellij.openapi.actionSystem.ActionPlaces.TEXT_EDITOR_WITH_PREVIEW;

/**
 * @author shuzijun
 */
public class SolutionPreview extends UserDataHolderBase implements FileEditor {

    private static final String MY_PROPORTION_KEY = PluginConstant.PLUGIN_ID + "SolutionSplitEditor.Proportion";


    private final Project project;
    private final LeetcodeEditor leetcodeEditor;


    private BorderLayoutPanel myComponent;
    private Question question;
    private FileEditor fileEditor;

    private boolean isLoad = false;

    private List<Solution> solutionList;
    private JBTable table;

    private JBSplitter mySplitter;
    private SplitFileEditor.SplitEditorLayout myLayout = SplitFileEditor.SplitEditorLayout.FIRST;

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
        ApplicationManager.getApplication().invokeLater(() -> {
            JBLabel loadingLabel = new JBLabel("Loading......");
            mySplitter.setFirstComponent(loadingLabel);
            try {
                question = QuestionManager.getQuestionByTitleSlug(leetcodeEditor.getTitleSlug(), project);

                if (question == null || Constant.ARTICLE_LIVE_NONE.equals(question.getArticleLive())) {
                    mySplitter.setFirstComponent(new JBLabel("No question or no solution"));
                } else if (Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
                    openArticle();
                    myLayout = SplitFileEditor.SplitEditorLayout.SECOND;
                    adjustEditorsVisibility();
                } else if (Constant.ARTICLE_LIVE_LIST.equals(question.getArticleLive())) {
                    solutionList = ApplicationManager.getApplication().executeOnPooledThread(() -> ArticleManager.getSolutionList(question.getTitleSlug(), project)).get();
                    if (CollectionUtils.isEmpty(solutionList)) {
                        mySplitter.setFirstComponent(new JBLabel("no solution"));
                    } else {
                        table = new JBTable(new TableModel(solutionList));
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        table.getTableHeader().setReorderingAllowed(false);
                        table.setRowSelectionAllowed(true);
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
                        mySplitter.setFirstComponent(jbScrollPane);
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
                    mySplitter.setFirstComponent(new JBLabel("no solution"));
                }
            } catch (Exception e) {
                myLayout = SplitFileEditor.SplitEditorLayout.FIRST;
                adjustEditorsVisibility();
                mySplitter.setFirstComponent(new JBLabel(e.getMessage()));
            } finally {
                mySplitter.remove(loadingLabel);
            }
        });
    }

    private void openSelectedQuestion(List<Solution> solutionList, int row) {
        if (row < 0 || solutionList == null || row >= solutionList.size()) {
            return;
        }
        Solution solution = solutionList.get(row);
        question.setArticleSlug(solution.getSlug());
        try {
            myLayout = SplitFileEditor.SplitEditorLayout.SPLIT;
            adjustEditorsVisibility();
            openArticle();
        } catch (Exception e) {
        }
    }

    private void openArticle() throws InterruptedException, java.util.concurrent.ExecutionException {
        File file = ApplicationManager.getApplication().executeOnPooledThread(() -> {
            return ArticleManager.openArticle(question.getTitleSlug(), question.getArticleSlug(), project, false);
        }).get();
        if (file == null || !file.exists()) {
            mySplitter.setSecondComponent(new JBLabel("no solution"));
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
            if (!Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
                secondComponent.addToTop(createToolbarWrapper(fileEditor.getComponent()));
            }
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
            } else if (CollectionUtils.isNotEmpty(solutionList)) {
                for (int i = 0; i < solutionList.size(); i++) {
                    if (solutionList.get(i).getSlug().equals(slug)) {
                        openSelectedQuestion(solutionList, i);
                        table.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
            if (myLayout == SplitFileEditor.SplitEditorLayout.SECOND || myLayout == SplitFileEditor.SplitEditorLayout.SPLIT) {
                try {
                    openArticle();
                } catch (Exception ignore) {
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
}
