package com.shuzijun.leetcode.plugin.utils;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class SharedPathFileWriterTest {

    @Test
    public void writesDirectlyToUserSelectedPathWithoutProductSubdirectory() throws Exception {
        Path directory = Files.createTempDirectory("shared-path");
        Path selectedFile = directory.resolve("solution.java");

        SharedPathFileWriter.write(selectedFile, "class Solution {}");

        assertEquals("class Solution {}", Files.readString(selectedFile));
        assertFalse(Files.exists(directory.resolve("leetcode")));
        assertFalse(Files.exists(directory.resolve("leetcode-pro")));
    }

    @Test
    public void rejectsSaveWhenAnotherPluginChangedTheFile() throws Exception {
        Path directory = Files.createTempDirectory("shared-path-conflict");
        Path selectedFile = directory.resolve("solution.java");
        Files.writeString(selectedFile, "version-one");
        String editingDigest = SharedPathFileWriter.digest(selectedFile);
        Files.writeString(selectedFile, "changed-by-other-plugin");

        assertThrows(SharedPathFileWriter.ConcurrentFileModificationException.class,
                () -> SharedPathFileWriter.write(selectedFile, "my-edit", editingDigest));
        assertEquals("changed-by-other-plugin", Files.readString(selectedFile));
    }

    @Test
    public void allowsSaveWhenDigestStillMatches() throws Exception {
        Path directory = Files.createTempDirectory("shared-path-match");
        Path selectedFile = directory.resolve("solution.java");
        Files.writeString(selectedFile, "version-one");
        String editingDigest = SharedPathFileWriter.digest(selectedFile);

        SharedPathFileWriter.write(selectedFile, "version-two", editingDigest);

        assertEquals("version-two", Files.readString(selectedFile));
    }
}
