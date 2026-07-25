package com.shuzijun.leetcode.plugin.setting;

import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ProjectConfigTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void prunesMissingSourceFilesOnly() throws Exception {
        Path project = temporaryFolder.newFolder("project").toPath();
        Path liveSource = Files.write(project.resolve("live.java"), "code".getBytes(StandardCharsets.UTF_8));
        Path liveContent = Files.write(project.resolve("live.lcv"), "content".getBytes(StandardCharsets.UTF_8));

        LeetcodeEditor live = editor("1", liveSource, liveContent);
        LeetcodeEditor missingSource = editor("2", project.resolve("deleted.java"), liveContent);
        LeetcodeEditor missingContent = editor("3", liveSource, project.resolve("deleted.lcv"));

        ProjectConfig.InnerState state = new ProjectConfig.InnerState();
        state.projectConfig.put(live.getPath(), live);
        state.projectConfig.put(missingSource.getPath(), missingSource);
        state.projectConfig.put(missingContent.getPath() + "-mapping", missingContent);

        ProjectConfig config = new ProjectConfig();
        config.loadState(state);

        assertEquals(1, config.pruneStaleEntries(project.toString()));
        assertSame(live, config.getEditor(live.getPath()));
        assertNull(config.getEditor(missingSource.getPath()));
        assertSame(missingContent, config.getEditor(missingContent.getPath() + "-mapping"));
        assertSame(live, config.getDefEditor("1"));
    }

    @Test
    public void prunesLargeStaleIndexInOnePass() throws Exception {
        Path project = temporaryFolder.newFolder("large-project").toPath();
        ProjectConfig.InnerState state = new ProjectConfig.InnerState();
        for (int i = 0; i < 2500; i++) {
            Path missing = project.resolve("deleted-" + i + ".java");
            LeetcodeEditor editor = editor(Integer.toString(i), missing, null);
            state.projectConfig.put(editor.getPath(), editor);
        }

        ProjectConfig config = new ProjectConfig();
        config.loadState(state);

        assertEquals(2500, config.pruneStaleEntries(project.toString()));
        assertEquals(0, config.getState().projectConfig.size());
    }

    @Test
    public void expandsProjectDirectoryMacro() throws Exception {
        Path project = temporaryFolder.newFolder("macro-project").toPath();
        assertEquals(project.resolve("src").resolve("answer.java").toAbsolutePath().normalize(),
                ProjectConfig.resolveLocalPath("$PROJECT_DIR$/src/answer.java", project.toString()));
    }

    @Test
    public void removesEditorWithProjectDirectoryMacro() throws Exception {
        Path project = temporaryFolder.newFolder("remove-macro-project").toPath();
        Path source = Files.write(project.resolve("answer.java"), "code".getBytes(StandardCharsets.UTF_8));
        LeetcodeEditor editor = editor("1", source, null);
        editor.setPath("$PROJECT_DIR$/answer.java");

        ProjectConfig.InnerState state = new ProjectConfig.InnerState();
        state.projectConfig.put(editor.getPath(), editor);

        ProjectConfig config = new ProjectConfig();
        config.loadState(state);

        assertEquals(true, config.removeEditor(source.toString(), project.toString()));
        assertEquals(0, config.getState().projectConfig.size());
    }

    private static LeetcodeEditor editor(String id, Path source, Path content) {
        LeetcodeEditor editor = new LeetcodeEditor();
        editor.setFrontendQuestionId(id);
        editor.setPath(source.toString());
        editor.setContentPath(content == null ? null : content.toString());
        return editor;
    }
}
