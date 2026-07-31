package com.shuzijun.leetcode.plugin.actions.toolbar;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClearAllActionTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void deletesOnlyTheConfiguredCacheTree() throws Exception {
        Path cacheRoot = temporaryFolder.newFolder("leetcode-cache").toPath();
        Path nestedDirectory = Files.createDirectories(cacheRoot.resolve("nested"));
        Path nestedFile = Files.write(nestedDirectory.resolve("answer.java"),
                "code".getBytes(StandardCharsets.UTF_8));
        Path sibling = Files.write(cacheRoot.getParent().resolve("keep.txt"),
                "keep".getBytes(StandardCharsets.UTF_8));

        ClearAllAction.deleteTree(cacheRoot);

        assertFalse(Files.exists(nestedFile));
        assertFalse(Files.exists(cacheRoot));
        assertTrue(Files.exists(sibling));
    }
}
