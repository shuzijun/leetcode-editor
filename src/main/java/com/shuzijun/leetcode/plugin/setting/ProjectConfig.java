package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
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
@State(name = "LeetcodeEditor" + PluginConstant.ACTION_SUFFIX, storages = {@Storage(value = PluginConstant.ACTION_PREFIX + "/editor.xml")})
public class ProjectConfig implements PersistentStateComponent<ProjectConfig.InnerState> {

    private static final Logger LOG = Logger.getInstance(ProjectConfig.class);
    private static final String PROJECT_DIR_MACRO = "$PROJECT_DIR$";

    private final ConcurrentMap<String, LeetcodeEditor> idProjectConfig = new ConcurrentHashMap<>();

    @Nullable
    public static ProjectConfig getInstance(Project project) {
        return project.getService(ProjectConfig.class);
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
            idProjectConfig.put(leetcodeEditor.getFrontendQuestionId(), leetcodeEditor);
        });
    }


    public LeetcodeEditor getDefEditor(String frontendQuestionId) {
        return idProjectConfig.computeIfAbsent(frontendQuestionId, key -> new LeetcodeEditor());
    }

    public void addLeetcodeEditor(LeetcodeEditor leetcodeEditor) {
        idProjectConfig.put(leetcodeEditor.getFrontendQuestionId(), leetcodeEditor);
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
                idProjectConfig.remove(editor.getFrontendQuestionId(), editor);
                removed++;
            }
        }

        // A frontend id can temporarily be associated with more than one path. Restore the live
        // value after removals without an O(n²) search for every stale entry.
        innerState.projectConfig.values().forEach(editor -> {
            if (StringUtils.isNotBlank(editor.getFrontendQuestionId())) {
                idProjectConfig.put(editor.getFrontendQuestionId(), editor);
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
            idProjectConfig.remove(editor.getFrontendQuestionId(), editor);
            return true;
        }
        return false;
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
