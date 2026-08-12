package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import com.intellij.icons.AllIcons;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.lc.model.CommonNote;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.manager.NoteManager;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.AsyncTaskHandle;
import com.shuzijun.leetcode.plugin.utils.AsyncUiUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.ui.ContentStatePanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shuzijun
 */
public class NotePreview extends UserDataHolderBase implements FileEditor {

    private static final String NOTE_OPERATION = "note-preview";

    private final Project project;
    private final LeetcodeEditor leetcodeEditor;


    private BorderLayoutPanel myComponent;
    private FileEditor fileEditor;
    private ContentStatePanel contentState;
    private final AtomicInteger requestId = new AtomicInteger();
    private AsyncTaskHandle loadTask;
    private List<CommonNote> commonNotes = Collections.emptyList();
    private CommonNote selectedCommonNote;
    private boolean updatingNoteSelector;

    private boolean isLoad = false;

    public NotePreview(Project project, LeetcodeEditor leetcodeEditor) {
        this.project = project;
        this.leetcodeEditor = leetcodeEditor;
    }

    @Override
    public @NotNull JComponent getComponent() {
        if (myComponent == null) {
            myComponent = JBUI.Panels.simplePanel();
            contentState = new ContentStatePanel();
            myComponent.addToCenter(contentState);
            if (isLoad) {
                initComponent();
            }
        }
        return myComponent;
    }


    private void initComponent() {
        isLoad = true;
        int currentRequestId = requestId.incrementAndGet();
        cancelLoad();
        disposeFileEditor();
        ApplicationManager.getApplication().invokeLater(() -> {
            if (currentRequestId != requestId.get()) {
                return;
            }
            contentState.showLoading(PropertiesUtils.getInfo("ui.loading"));
            loadTask = AsyncUiUtils.load(project, this, NOTE_OPERATION, () -> {
                if (!LeetCodeServices.login().isLoggedIn()) {
                    return NoteLoadResult.loggedOut();
                }
                CodeTypeEnum codeTypeEnum =
                        CodeTypeEnum.getCodeTypeEnumByLangSlug(leetcodeEditor.getLangSlug());
                if (URLUtils.leetcodecn.equals(leetcodeEditor.getHost())) {
                    Question question = QuestionManager.getQuestionByTitleSlug(
                            leetcodeEditor.getTitleSlug(),
                            project
                    );
                    List<CommonNote> notes = NoteManager.commonNotes(question);
                    CommonNote selected = NoteManager.selectedCommonNote(question, notes);
                    File file = NoteManager.showCommonNote(
                            question,
                            selected,
                            project,
                            codeTypeEnum
                    );
                    return new NoteLoadResult(toVirtualFile(file), notes, selected);
                }
                File file = NoteManager.show(
                        leetcodeEditor.getTitleSlug(),
                        project,
                        false,
                        codeTypeEnum
                );
                return new NoteLoadResult(
                        toVirtualFile(file),
                        Collections.emptyList(),
                        null
                );
            }, (result, error) -> {
                if (currentRequestId != requestId.get()) {
                    return;
                }
                if (error != null) {
                    contentState.showError(
                            PropertiesUtils.getInfo("ui.note.failed"),
                            PropertiesUtils.getInfo("ui.retry"),
                            this::initComponent
                    );
                } else if (result == null) {
                    contentState.showError(
                            PropertiesUtils.getInfo("ui.note.failed"),
                            PropertiesUtils.getInfo("ui.retry"),
                            this::initComponent
                    );
                } else if (!result.loggedIn) {
                    showLoginRequired();
                } else if (result.file == null) {
                    contentState.showEmpty(PropertiesUtils.getInfo("ui.note.empty"), null, null);
                } else {
                    commonNotes = result.notes;
                    selectedCommonNote = result.selected;
                    showEditor(result.file);
                }
                myComponent.revalidate();
                myComponent.repaint();
            });
        });
    }

    private JComponent createToolbarWrapper(JComponent targetComponentForActions) {
        DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance().getAction(PluginConstant.LEETCODE_EDITOR_NOTE);
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Note" + ActionPlaces.TOOLBAR, actionGroup, true);
        actionToolbar.setTargetComponent(targetComponentForActions);
        SplitEditorToolbar splitEditorToolbar = new SplitEditorToolbar(null, actionToolbar);
        if (URLUtils.leetcodecn.equals(leetcodeEditor.getHost())) {
            JPanel panel = new JPanel(new BorderLayout(JBUI.scale(8), 0));
            panel.setBorder(JBUI.Borders.empty(2, 6));
            panel.add(createCommonNoteControls(), BorderLayout.CENTER);
            panel.add(splitEditorToolbar, BorderLayout.EAST);
            return panel;
        }
        return splitEditorToolbar;
    }

    private JComponent createCommonNoteControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(4), 0));
        JComboBox<CommonNoteItem> selector = new JComboBox<>();
        selector.setPrototypeDisplayValue(new CommonNoteItem(null, "xxxxxxxxxxxxxxxxxxxxxxxx"));
        updatingNoteSelector = true;
        selector.addItem(new CommonNoteItem(null, PropertiesUtils.getInfo("ui.note.new")));
        for (CommonNote note : commonNotes) {
            selector.addItem(new CommonNoteItem(note, noteTitle(note)));
        }
        selector.setSelectedIndex(selectedCommonNote == null ? 0 : selectedNoteIndex());
        updatingNoteSelector = false;
        selector.addActionListener(event -> {
            if (updatingNoteSelector) {
                return;
            }
            CommonNoteItem item = (CommonNoteItem) selector.getSelectedItem();
            if (item == null || item.note == null) {
                createCommonNote();
            } else {
                loadCommonNote(item.note);
            }
        });

        JButton add = iconButton(AllIcons.General.Add, PropertiesUtils.getInfo("ui.note.new"));
        add.addActionListener(event -> createCommonNote());
        JButton delete = iconButton(AllIcons.General.Remove, PropertiesUtils.getInfo("ui.note.delete"));
        delete.setEnabled(selectedCommonNote != null);
        delete.addActionListener(event -> deleteSelectedCommonNote());
        JButton refresh = iconButton(AllIcons.Actions.Refresh, PropertiesUtils.getInfo("ui.note.refresh"));
        refresh.addActionListener(event -> initComponent());

        controls.add(selector);
        controls.add(add);
        controls.add(delete);
        controls.add(refresh);
        return controls;
    }

    private void createCommonNote() {
        int currentRequestId = requestId.incrementAndGet();
        cancelLoad();
        contentState.showLoading(PropertiesUtils.getInfo("ui.loading"));
        loadTask = AsyncUiUtils.load(project, this, NOTE_OPERATION, () -> {
            if (!LeetCodeServices.login().isLoggedIn()) {
                return NoteOperationResult.<CommonNote>loggedOut();
            }
            Question question = QuestionManager.getQuestionByTitleSlug(
                    leetcodeEditor.getTitleSlug(),
                    project
            );
            return NoteOperationResult.loggedIn(NoteManager.createCommonNote(question));
        }, (result, error) -> {
            if (currentRequestId != requestId.get()) {
                return;
            }
            if (error == null && result != null && !result.loggedIn) {
                showLoginRequired();
                return;
            }
            if (error != null || result == null || result.value == null) {
                MessageUtils.getInstance(project).showWarnMsg(
                        "error",
                        PropertiesUtils.getInfo("request.failed")
                );
            }
            initComponent();
        });
    }

    private void loadCommonNote(CommonNote note) {
        int currentRequestId = requestId.incrementAndGet();
        cancelLoad();
        contentState.showLoading(PropertiesUtils.getInfo("ui.loading"));
        loadTask = AsyncUiUtils.load(project, this, NOTE_OPERATION, () -> {
            if (!LeetCodeServices.login().isLoggedIn()) {
                return NoteOperationResult.<VirtualFile>loggedOut();
            }
            Question question = QuestionManager.getQuestionByTitleSlug(
                    leetcodeEditor.getTitleSlug(),
                    project
            );
            CodeTypeEnum codeTypeEnum =
                    CodeTypeEnum.getCodeTypeEnumByLangSlug(leetcodeEditor.getLangSlug());
            File file = NoteManager.showCommonNote(question, note, project, codeTypeEnum);
            return NoteOperationResult.loggedIn(toVirtualFile(file));
        }, (result, error) -> {
            if (currentRequestId != requestId.get()) {
                return;
            }
            if (error == null && result != null && !result.loggedIn) {
                showLoginRequired();
                return;
            }
            if (error != null || result == null || result.value == null) {
                contentState.showError(
                        PropertiesUtils.getInfo("ui.note.failed"),
                        PropertiesUtils.getInfo("ui.retry"),
                        () -> loadCommonNote(note)
                );
                return;
            }
            selectedCommonNote = note;
            showEditor(result.value);
        });
    }

    private void deleteSelectedCommonNote() {
        CommonNote note = selectedCommonNote;
        if (note == null) {
            return;
        }
        if (Messages.showYesNoDialog(
                project,
                PropertiesUtils.getInfo("ui.note.delete.confirm"),
                PropertiesUtils.getInfo("ui.note.delete"),
                Messages.getQuestionIcon()
        ) != Messages.YES) {
            return;
        }
        int currentRequestId = requestId.incrementAndGet();
        cancelLoad();
        contentState.showLoading(PropertiesUtils.getInfo("ui.loading"));
        loadTask = AsyncUiUtils.load(project, this, NOTE_OPERATION, () -> {
            if (!LeetCodeServices.login().isLoggedIn()) {
                return NoteOperationResult.<Boolean>loggedOut();
            }
            Question question = QuestionManager.getQuestionByTitleSlug(
                    leetcodeEditor.getTitleSlug(),
                    project
            );
            return NoteOperationResult.loggedIn(NoteManager.deleteCommonNote(question, note));
        }, (result, error) -> {
            if (currentRequestId != requestId.get()) {
                return;
            }
            if (error == null && result != null && !result.loggedIn) {
                showLoginRequired();
                return;
            }
            if (error != null || result == null || !Boolean.TRUE.equals(result.value)) {
                MessageUtils.getInstance(project).showWarnMsg(
                        "error",
                        PropertiesUtils.getInfo("request.failed")
                );
            }
            initComponent();
        });
    }

    private void showEditor(VirtualFile file) {
        List<FileEditorProvider> editorProviders =
                FileEditorProviderManager.getInstance().getProviderList(project, file);
        disposeFileEditor();
        if (!editorProviders.isEmpty()) {
            fileEditor = editorProviders.get(0).createEditor(project, file);
        } else {
            fileEditor = new PsiAwareTextEditorProvider().createEditor(project, file);
        }
        Disposer.register(this, fileEditor);
        BorderLayoutPanel editorPanel = JBUI.Panels.simplePanel(fileEditor.getComponent());
        editorPanel.addToTop(createToolbarWrapper(fileEditor.getComponent()));
        contentState.showContent(editorPanel);
        myComponent.revalidate();
        myComponent.repaint();
    }

    private void showLoginRequired() {
        cancelLoad();
        disposeFileEditor();
        commonNotes = Collections.emptyList();
        selectedCommonNote = null;
        contentState.showLoginRequired(
                PropertiesUtils.getInfo("login.not"),
                PropertiesUtils.getInfo("ui.sign.in"),
                null
        );
    }

    private int selectedNoteIndex() {
        for (int index = 0; index < commonNotes.size(); index++) {
            if (commonNotes.get(index).getId().equals(selectedCommonNote.getId())) {
                return index + 1;
            }
        }
        return 0;
    }

    private static String noteTitle(CommonNote note) {
        String title = note.getSummary();
        if (title == null || title.trim().isEmpty()) {
            title = note.getContent();
        }
        if (title == null || title.trim().isEmpty()) {
            return note.getId();
        }
        String firstLine = title.trim().split("\\R", 2)[0];
        return firstLine.length() > 40 ? firstLine.substring(0, 40) + "..." : firstLine;
    }

    private static JButton iconButton(Icon icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setPreferredSize(JBUI.size(28, 28));
        return button;
    }

    private static VirtualFile toVirtualFile(File file) {
        return file == null || !file.exists()
                ? null
                : LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return PluginConstant.LEETCODE_EDITOR_TAB_VIEW + " Note";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (state instanceof ConvergePreview.TabFileEditorState) {
            if (!isLoad && ((ConvergePreview.TabFileEditorState) state).isLoad()) {
                initComponent();
            } else {
                if (fileEditor != null) {
                    refreshFile(fileEditor.getFile());
                }
            }
        } else if (state instanceof ConvergePreview.TabSelectFileEditorState) {
            ConvergePreview.TabSelectFileEditorState tabState =
                    (ConvergePreview.TabSelectFileEditorState) state;
            if ("refresh".equals(tabState.getChildrenState())) {
                initComponent();
            }
        } else if (state instanceof ConvergePreview.LoginState) {
            ConvergePreview.LoginState loginState = (ConvergePreview.LoginState) state;
            if (isLoad) {
                if (loginState.isSelect()) {
                    if (loginState.isLogin()) {
                        ApplicationManager.getApplication().invokeLater(this::initComponent);
                    } else {
                        showLoginRequired();
                    }
                } else {
                    isLoad = false;
                }
            }
        }
    }

    private static void refreshFile(VirtualFile file) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (file.isValid()) {
                RefreshQueue.getInstance().refresh(false, false, null, file);
            }
        });
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
        requestId.incrementAndGet();
        cancelLoad();
        disposeFileEditor();
    }

    private void cancelLoad() {
        if (loadTask != null) {
            loadTask.cancel();
            loadTask = null;
        }
    }

    private void disposeFileEditor() {
        if (fileEditor != null) {
            Disposer.dispose(fileEditor);
            fileEditor = null;
        }
    }

    private static final class NoteLoadResult {
        private final boolean loggedIn;
        private final VirtualFile file;
        private final List<CommonNote> notes;
        private final CommonNote selected;

        private NoteLoadResult(
                boolean loggedIn,
                VirtualFile file,
                List<CommonNote> notes,
                CommonNote selected
        ) {
            this.loggedIn = loggedIn;
            this.file = file;
            this.notes = notes;
            this.selected = selected;
        }

        private NoteLoadResult(
                VirtualFile file,
                List<CommonNote> notes,
                CommonNote selected
        ) {
            this(true, file, notes, selected);
        }

        private static NoteLoadResult loggedOut() {
            return new NoteLoadResult(false, null, Collections.emptyList(), null);
        }
    }

    private static final class NoteOperationResult<T> {
        private final boolean loggedIn;
        private final T value;

        private NoteOperationResult(boolean loggedIn, T value) {
            this.loggedIn = loggedIn;
            this.value = value;
        }

        private static <T> NoteOperationResult<T> loggedIn(T value) {
            return new NoteOperationResult<>(true, value);
        }

        private static <T> NoteOperationResult<T> loggedOut() {
            return new NoteOperationResult<>(false, null);
        }
    }

    private static final class CommonNoteItem {
        private final CommonNote note;
        private final String title;

        private CommonNoteItem(CommonNote note, String title) {
            this.note = note;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
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
