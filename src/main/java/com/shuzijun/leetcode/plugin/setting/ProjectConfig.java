package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shuzijun
 */
public class ProjectConfig implements PersistentStateComponent<ProjectConfig.InnerState> {

    private static final Logger LOG = Logger.getInstance(ProjectConfig.class);
    private static final String PROJECT_DIR_MACRO = "$PROJECT_DIR$";

    private final ConcurrentMap<String, LeetcodeEditor> idProjectConfig = new ConcurrentHashMap<>();
    private volatile String lastOpenedQuestionTitleSlug;

    @Nullable
    public static ProjectConfig getInstance(Project project) {
        return ProductServices.projectConfig(project);
    }

    private volatile InnerState innerState = new InnerState();

    @Nullable
    @Override
    public ProjectConfig.InnerState getState() {
        return innerState;
    }

    @Override
    public void loadState(@NotNull ProjectConfig.InnerState innerState) {
        innerState.projectConfig = new ConcurrentHashMap<>(innerState.projectConfig);
        this.innerState = innerState;
        idProjectConfig.clear();
        this.innerState.projectConfig.forEach((key, leetcodeEditor) -> {
            if (StringUtils.isBlank(leetcodeEditor.getFrontendQuestionId())) {
                this.innerState.projectConfig.remove(key, leetcodeEditor);
                return;
            } else if (leetcodeEditor.getFrontendQuestionId().startsWith(URLUtils.leetcodecnOld)) {
                leetcodeEditor.setHost(URLUtils.leetcodecn);
                leetcodeEditor.setFrontendQuestionId(leetcodeEditor.getFrontendQuestionId().replace(URLUtils.leetcodecnOld, URLUtils.leetcodecn));
            }
            if (StringUtils.isBlank(leetcodeEditor.getLangSlug())) {
                leetcodeEditor.setLangSlug(inferLanguageSlug(editorPath(leetcodeEditor, key)));
            }
            idProjectConfig.put(editorKey(leetcodeEditor), leetcodeEditor);
        });
    }


    public LeetcodeEditor getDefEditor(String frontendQuestionId) {
        return getDefEditor(frontendQuestionId, null);
    }

    public LeetcodeEditor getDefEditor(String frontendQuestionId, @Nullable String langSlug) {
        String key = editorKey(frontendQuestionId, langSlug);
        LeetcodeEditor editor = idProjectConfig.get(key);
        if (editor != null) {
            return editor;
        }
        if (StringUtils.isBlank(langSlug)) {
            for (LeetcodeEditor candidate : idProjectConfig.values()) {
                if (frontendQuestionId.equals(candidate.getFrontendQuestionId())) {
                    return candidate;
                }
            }
        }
        return idProjectConfig.computeIfAbsent(key, ignored -> {
            LeetcodeEditor created = new LeetcodeEditor();
            created.setFrontendQuestionId(frontendQuestionId);
            created.setLangSlug(langSlug);
            return created;
        });
    }

    public void addLeetcodeEditor(LeetcodeEditor leetcodeEditor) {
        idProjectConfig.put(editorKey(leetcodeEditor), leetcodeEditor);
        if (StringUtils.isNotBlank(leetcodeEditor.getPath())) {
            innerState.projectConfig.put(leetcodeEditor.getPath(), leetcodeEditor);
        }
    }

    public LeetcodeEditor getEditor(String path) {
        return innerState.projectConfig.get(path);
    }

    public LeetcodeEditor getEditor(String path, String host) {
        LeetcodeEditor leetcodeEditor = innerState.projectConfig.get(path);
        if (leetcodeEditor != null && host.equals(leetcodeEditor.getHost())) {
            return leetcodeEditor;
        } else {
            return null;
        }
    }

    @Nullable
    public String getLastOpenedQuestionTitleSlug() {
        return lastOpenedQuestionTitleSlug;
    }

    public void setLastOpenedQuestionTitleSlug(@Nullable String titleSlug) {
        this.lastOpenedQuestionTitleSlug = titleSlug;
    }

    /**
     * Removes persisted editor records whose source files no longer exist. This is intentionally
     * called from a background project activity because large, long-lived projects can accumulate
     * thousands of obsolete records.
     */
    public int pruneStaleEntries(@Nullable String projectBasePath) {
        int removed = 0;
        for (Map.Entry<String, LeetcodeEditor> entry : innerState.projectConfig.entrySet()) {
            LeetcodeEditor editor = entry.getValue();
            String editorPath = StringUtils.defaultIfBlank(editor.getPath(), entry.getKey());
            Path localPath = resolveLocalPath(editorPath, projectBasePath);
            boolean sourceMissing = localPath != null && !Files.isRegularFile(localPath);
            if (sourceMissing
                    && innerState.projectConfig.remove(entry.getKey(), editor)) {
                idProjectConfig.remove(editorKey(editor), editor);
                removed++;
            }
        }

        // A frontend id can temporarily be associated with more than one path. Restore the live
        // value after removals without an O(n²) search for every stale entry.
        innerState.projectConfig.values().forEach(editor -> {
            if (StringUtils.isNotBlank(editor.getFrontendQuestionId())) {
                idProjectConfig.put(editorKey(editor), editor);
            }
        });
        if (removed > 0) {
            LOG.info("Pruned " + removed + " stale LeetCode editor records");
        }
        return removed;
    }

    public boolean removeEditor(String path) {
        return removeEditor(path, null);
    }

    public boolean removeEditor(String path, @Nullable String projectBasePath) {
        LeetcodeEditor editor = innerState.projectConfig.remove(path);
        if (editor == null) {
            Path target = resolveLocalPath(path, projectBasePath);
            if (target != null) {
                for (Map.Entry<String, LeetcodeEditor> entry : innerState.projectConfig.entrySet()) {
                    String editorPath = StringUtils.defaultIfBlank(entry.getValue().getPath(), entry.getKey());
                    if (target.equals(resolveLocalPath(editorPath, projectBasePath))
                            && innerState.projectConfig.remove(entry.getKey(), entry.getValue())) {
                        editor = entry.getValue();
                        break;
                    }
                }
            }
        }
        if (editor != null) {
            idProjectConfig.remove(editorKey(editor), editor);
            return true;
        }
        return false;
    }

    @Nullable
    private static String inferLanguageSlug(String editorPath) {
        for (CodeTypeEnum codeType : CodeTypeEnum.values()) {
            if (StringUtils.endsWith(editorPath, codeType.getSuffix())) {
                return codeType.getLangSlug();
            }
        }
        return null;
    }

    private static String editorPath(LeetcodeEditor editor, String persistedKey) {
        return StringUtils.defaultIfBlank(editor.getPath(), persistedKey);
    }

    private static String editorKey(LeetcodeEditor editor) {
        return editorKey(editor.getFrontendQuestionId(), editor.getLangSlug());
    }

    private static String editorKey(String frontendQuestionId, @Nullable String langSlug) {
        return StringUtils.defaultString(langSlug) + '\u0000' + frontendQuestionId;
    }

    @Nullable
    static Path resolveLocalPath(@Nullable String path, @Nullable String projectBasePath) {
        if (StringUtils.isBlank(path) || path.contains("://")) {
            return null;
        }
        String expanded = path;
        if (expanded.contains(PROJECT_DIR_MACRO)) {
            if (StringUtils.isBlank(projectBasePath)) {
                return null;
            }
            expanded = expanded.replace(PROJECT_DIR_MACRO, projectBasePath);
        }
        try {
            return Paths.get(expanded).toAbsolutePath().normalize();
        } catch (InvalidPathException ignored) {
            return null;
        }
    }

    public static class InnerState {
        @NotNull
        @MapAnnotation
        public Map<String, LeetcodeEditor> projectConfig;

        InnerState() {
            projectConfig = new ConcurrentHashMap<>();
        }
    }

    public String getComponentName() {
        return this.getClass().getName();
    }

}
