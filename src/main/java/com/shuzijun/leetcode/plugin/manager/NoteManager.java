package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.lc.model.CommonNote;
import com.shuzijun.lc.model.CommonNotePage;
import com.shuzijun.lc.model.CommonNoteResult;
import com.shuzijun.lc.model.NoteUpdateResult;
import com.shuzijun.leetcode.plugin.application.LanguageTemplateService;
import com.shuzijun.leetcode.plugin.application.LeetCodeNoteService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shuzijun
 */
public class NoteManager {

    private static final ConcurrentMap<String, String> SELECTED_COMMON_NOTES =
            new ConcurrentHashMap<>();
    private static final Set<String> NEW_COMMON_NOTES = ConcurrentHashMap.newKeySet();

    public static File show(String titleSlug, Project project, Boolean isOpenEditor) {
        Config config = PersistentConfig.getInstance().getConfig();
        return show(titleSlug, project, isOpenEditor, config.getCodeTypeEnum(project));
    }

    public static File show(String titleSlug, Project project, Boolean isOpenEditor, CodeTypeEnum codeTypeEnum) {
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        String filePath = PersistentConfig.getInstance().getTempFilePath()
                + Constant.DOC_NOTE
                + LanguageTemplateService.fileName("note", question)
                + ".md";
        File file = new File(filePath);
        if (file.exists()) {
            if (isOpenEditor) {
                FileUtils.openFileEditor(file, project);
            }
        } else {
            if (pull(titleSlug, project, codeTypeEnum)) {
                if (isOpenEditor) {
                    FileUtils.openFileEditor(file, project);
                }
            }
        }
        return file;
    }

    public static boolean pull(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getConfig();
        return pull(titleSlug, project, config.getCodeTypeEnum(project));
    }

    public static boolean pull(String titleSlug, Project project, CodeTypeEnum codeTypeEnum) {
        try {
            if (!LeetCodeServices.login().isLoggedIn()) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return false;
            }
            Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
            String filePath = PersistentConfig.getInstance().getTempFilePath()
                    + Constant.DOC_NOTE
                    + LanguageTemplateService.fileName("note", question)
                    + ".md";

            String note;
            if (URLUtils.isCn()) {
                List<CommonNote> notes = commonNotes(question);
                CommonNote selected = notes.isEmpty() ? null : notes.get(0);
                selectCommonNote(question.getTitleSlug(), selected);
                note = selected == null ? null : selected.getContent();
            } else {
                note = noteService().get(question.getTitleSlug());
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(note)) {
                note = ProductServices.noteContentStrategy().initialContent(question, codeTypeEnum);
            }
            FileUtils.saveFile(filePath, note);
            return Boolean.TRUE;
        } catch (Exception e) {
            LogUtils.LOG.error("pull note error", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
        return Boolean.FALSE;
    }

    public static void push(String titleSlug, Project project) {
        try {
            if (!LeetCodeServices.login().isLoggedIn()) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return;
            }

            Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
            String filePath = PersistentConfig.getInstance().getTempFilePath()
                    + Constant.DOC_NOTE
                    + LanguageTemplateService.fileName("note", question)
                    + ".md";
            File file = new File(filePath);
            if (!file.exists()) {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.code"));
                return;
            }
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            FileUtils.saveEditDocument(vf);
            String note = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> FileDocumentManager.getInstance().getDocument(vf).getText());
            if (URLUtils.isCn()) {
                CommonNoteResult result = saveCommonNote(question, note);
                if (!result.isSuccess()) {
                    MessageUtils.getInstance(project).showWarnMsg(
                            "error",
                            PropertiesUtils.getInfo("request.failed")
                    );
                } else {
                    selectCommonNote(question.getTitleSlug(), result.getNote());
                    MessageUtils.getInstance(project).showInfoMsg("info", "success");
                }
            } else {
                NoteUpdateResult result = noteService()
                        .update(question.getTitleSlug(), note);
                if (!result.isSuccess()) {
                    MessageUtils.getInstance(project).showWarnMsg("error", result.getError());
                } else {
                    MessageUtils.getInstance(project).showInfoMsg("info", "success");
                }
            }
        } catch (Exception e) {
            LogUtils.LOG.error("push note error", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
    }

    public static List<CommonNote> commonNotes(Question question) throws Exception {
        if (!URLUtils.isCn()) {
            return Collections.emptyList();
        }
        CommonNotePage page = noteService().list(question.getQuestionId());
        return page.getNotes();
    }

    public static CommonNote selectedCommonNote(Question question, List<CommonNote> notes) {
        String key = commonNoteKey(question.getTitleSlug());
        if (NEW_COMMON_NOTES.contains(key)) {
            return null;
        }
        String selectedNoteId = SELECTED_COMMON_NOTES.get(key);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(selectedNoteId)) {
            for (CommonNote note : notes) {
                if (selectedNoteId.equals(note.getId())) {
                    return note;
                }
            }
            SELECTED_COMMON_NOTES.remove(key);
        }
        return notes.isEmpty() ? null : notes.get(0);
    }

    public static CommonNote createCommonNote(Question question) throws Exception {
        CommonNoteResult result = noteService().create(question.getQuestionId(), "", "");
        CommonNote note = result.getNote();
        if (!result.isSuccess()
                || note == null
                || org.apache.commons.lang3.StringUtils.isBlank(note.getId())) {
            return null;
        }
        selectCommonNote(question.getTitleSlug(), note);
        return note;
    }

    public static void selectCommonNote(String titleSlug, CommonNote note) {
        String key = commonNoteKey(titleSlug);
        if (note == null || org.apache.commons.lang3.StringUtils.isBlank(note.getId())) {
            SELECTED_COMMON_NOTES.remove(key);
            NEW_COMMON_NOTES.add(key);
        } else {
            SELECTED_COMMON_NOTES.put(key, note.getId());
            NEW_COMMON_NOTES.remove(key);
        }
    }

    public static File showCommonNote(
            Question question,
            CommonNote note,
            Project project,
            CodeTypeEnum codeTypeEnum
    ) {
        String content = note == null ? null : note.getContent();
        if (org.apache.commons.lang3.StringUtils.isBlank(content)) {
            content = ProductServices.noteContentStrategy().initialContent(question, codeTypeEnum);
        }
        String filePath = noteFilePath(question);
        saveNoteFile(filePath, content);
        selectCommonNote(question.getTitleSlug(), note);
        return new File(filePath);
    }

    public static boolean deleteCommonNote(Question question, CommonNote note) throws Exception {
        if (note == null || org.apache.commons.lang3.StringUtils.isBlank(note.getId())) {
            return false;
        }
        boolean deleted = noteService().delete(note.getId());
        if (deleted && note.getId().equals(SELECTED_COMMON_NOTES.get(commonNoteKey(question.getTitleSlug())))) {
            SELECTED_COMMON_NOTES.remove(commonNoteKey(question.getTitleSlug()));
            NEW_COMMON_NOTES.remove(commonNoteKey(question.getTitleSlug()));
        }
        return deleted;
    }

    private static CommonNoteResult saveCommonNote(Question question, String content) throws Exception {
        String key = commonNoteKey(question.getTitleSlug());
        String noteId = SELECTED_COMMON_NOTES.get(key);
        String summary = content == null ? "" : content;
        if (org.apache.commons.lang3.StringUtils.isBlank(noteId) && !NEW_COMMON_NOTES.contains(key)) {
            List<CommonNote> notes = commonNotes(question);
            if (!notes.isEmpty()) {
                noteId = notes.get(0).getId();
            }
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(noteId)) {
            return noteService().create(question.getQuestionId(), content, summary);
        }
        return noteService().updateCommon(noteId, content, summary);
    }

    private static String commonNoteKey(String titleSlug) {
        return URLUtils.getLeetcodeHost() + ":" + titleSlug;
    }

    private static String noteFilePath(Question question) {
        return PersistentConfig.getInstance().getTempFilePath()
                + Constant.DOC_NOTE
                + LanguageTemplateService.fileName("note", question)
                + ".md";
    }

    private static void saveNoteFile(String filePath, String content) {
        File file = new File(filePath);
        Document document = ReadAction.compute(() -> {
            VirtualFile virtualFile =
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            return virtualFile == null
                    ? null
                    : FileDocumentManager.getInstance().getDocument(virtualFile);
        });
        if (document == null) {
            FileUtils.saveFile(file, content);
            return;
        }
        WriteAction.runAndWait(() -> {
            document.setText(content);
            FileDocumentManager.getInstance().saveDocument(document);
        });
    }

    private static LeetCodeNoteService noteService() {
        return LeetCodeServices.note();
    }
}
