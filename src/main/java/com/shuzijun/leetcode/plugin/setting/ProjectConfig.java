package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
@State(name = "LeetcodeEditor" + PluginConstant.ACTION_SUFFIX, storages = {@Storage(value = PluginConstant.ACTION_PREFIX+"/editor.xml")})
public class ProjectConfig implements  PersistentStateComponent<ProjectConfig.InnerState> {

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
        this.innerState.projectConfig.forEach((s, leetcodeEditor) -> {
            idProjectConfig.put(leetcodeEditor.getQuestionId(),leetcodeEditor);
        });
    }


    public LeetcodeEditor getDefEditor(String questionId) {
        LeetcodeEditor leetcodeEditor = idProjectConfig.get(questionId);
        if (leetcodeEditor == null) {
            leetcodeEditor = new LeetcodeEditor();
            idProjectConfig.put(questionId,leetcodeEditor);
        }
        return leetcodeEditor;
    }

    public void addLeetcodeEditor(LeetcodeEditor leetcodeEditor) {
        idProjectConfig.put(leetcodeEditor.getQuestionId(), leetcodeEditor);
        if(StringUtils.isNotBlank(leetcodeEditor.getPath())) {
            innerState.projectConfig.put(leetcodeEditor.getPath(), leetcodeEditor);
        }
    }

    public LeetcodeEditor getEditor(String path) {
        return innerState.projectConfig.get(path);
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
