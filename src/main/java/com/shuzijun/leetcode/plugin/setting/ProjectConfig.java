package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author shuzijun
 */
@State(name = "LeetcodeEditor" + PluginConstant.ACTION_SUFFIX, storages = {@Storage(value = PluginConstant.ACTION_PREFIX + "/editor.xml")})
public class ProjectConfig implements PersistentStateComponent<ProjectConfig.InnerState> {

    public Map<String, LeetcodeEditor> idProjectConfig = new HashMap<>();

    @Nullable
    public static ProjectConfig getInstance(Project project) {
        return project.getService(ProjectConfig.class);
    }

    private InnerState innerState = new InnerState();

    @Nullable
    @Override
    public ProjectConfig.InnerState getState() {
        return innerState;
    }

    @Override
    public void loadState(@NotNull ProjectConfig.InnerState innerState) {
        this.innerState = innerState;
        idProjectConfig.clear();
        Iterator<String> iter = this.innerState.projectConfig.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            LeetcodeEditor leetcodeEditor = this.innerState.projectConfig.get(key);
            if (StringUtils.isBlank(leetcodeEditor.getFrontendQuestionId())) {
                iter.remove();
                continue;
            } else if (leetcodeEditor.getFrontendQuestionId().startsWith(URLUtils.leetcodecnOld)) {
                leetcodeEditor.setHost(URLUtils.leetcodecn);
                leetcodeEditor.setFrontendQuestionId(leetcodeEditor.getFrontendQuestionId().replace(URLUtils.leetcodecnOld, URLUtils.leetcodecn));
            }
            idProjectConfig.put(leetcodeEditor.getFrontendQuestionId(), leetcodeEditor);
        }
    }


    public LeetcodeEditor getDefEditor(String frontendQuestionId) {
        LeetcodeEditor leetcodeEditor = idProjectConfig.get(frontendQuestionId);
        if (leetcodeEditor == null) {
            leetcodeEditor = new LeetcodeEditor();
            idProjectConfig.put(frontendQuestionId, leetcodeEditor);
        }
        return leetcodeEditor;
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

    public static class InnerState {
        @NotNull
        @MapAnnotation
        public Map<String, LeetcodeEditor> projectConfig;

        InnerState() {
            projectConfig = new HashMap<>();
        }
    }

    public String getComponentName() {
        return this.getClass().getName();
    }

}
