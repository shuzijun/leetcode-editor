package com.shuzijun.leetcode.plugin.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class SharedPathFileWriter {

    private SharedPathFileWriter() {
    }

    @NotNull
    public static String digest(@NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            return "";
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static void write(@NotNull Path path, @NotNull String content) throws IOException {
        write(path, content, null);
    }

    public static void write(@NotNull Path path, @NotNull String content, @Nullable String expectedDigest)
            throws IOException {
        Path absolutePath = path.toAbsolutePath().normalize();
        Path parent = absolutePath.getParent();
        if (parent == null) {
            throw new IOException("A parent directory is required: " + path);
        }
        Files.createDirectories(parent);

        String currentDigest = digest(absolutePath);
        if (expectedDigest != null && !expectedDigest.equals(currentDigest)) {
            throw new ConcurrentFileModificationException(absolutePath, expectedDigest, currentDigest);
        }

        Path temporary = Files.createTempFile(parent, "." + absolutePath.getFileName(), ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, absolutePath, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static final class ConcurrentFileModificationException extends IOException {

        public ConcurrentFileModificationException(Path path, String expectedDigest, String actualDigest) {
            super("File changed before save: " + path + " expected=" + expectedDigest + " actual=" + actualDigest);
        }
    }
}
