package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Constant;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author shuzijun
 */
public class FileUtils {


    public static void saveFile(String path, String body) {
        saveFile(new File(path), body);
    }

    public static void saveFile(File file, String body) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, Boolean.FALSE);
            fileOutputStream.write(body.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (IOException io) {
            LogUtils.LOG.error("保存文件错误", io);
        }
    }

    public static String getFileBody(String filePath) {
        return getFileBody(new File(filePath));
    }

    public static String getFileBody(File file) {
        String all = "";
        if (file.exists()) {
            Long filelength = file.length();
            byte[] filecontent = new byte[filelength.intValue()];
            try {
                FileInputStream in = new FileInputStream(file);
                in.read(filecontent);
                in.close();
                all = new String(filecontent, "UTF-8");
            } catch (IOException i) {
                LogUtils.LOG.error("读取文件错误", i);

            }
        }
        return all;
    }

    public static String getClearCommentFileBody(File file, CodeTypeEnum codeTypeEnum) {

        VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (FileDocumentManager.getInstance().isFileModified(vf)) {
            try {
                WriteAction.computeAndWait(new ThrowableComputable<Boolean, Throwable>() {
                    @Override
                    public Boolean compute() throws Throwable {
                        FileDocumentManager.getInstance().saveDocument(FileDocumentManager.getInstance().getDocument(vf));
                        return true;
                    }
                });
            } catch (Throwable ignore) {
                LogUtils.LOG.error("自动保存文件错误", ignore);
            }

        }

        StringBuffer code = new StringBuffer();
        try {
            String body = VfsUtil.loadText(vf);
            if (StringUtils.isNotBlank(body)) {

                List<String> codeList = new LinkedList<>();
                int codeBegin = -1;
                int codeEnd = -1;
                int lineCount = 0;

                String[] lines = body.split("\r\n|\r|\n");
                for (String line : lines) {
                    if (StringUtils.isNotBlank(line) && trim(line).equals(codeTypeEnum.getComment() + trim(Constant.SUBMIT_REGION_BEGIN))) {
                        codeBegin = lineCount;
                    } else if (StringUtils.isNotBlank(line) && trim(line).equals(codeTypeEnum.getComment() + trim(Constant.SUBMIT_REGION_END))) {
                        codeEnd = lineCount;
                    }
                    codeList.add(line);
                    lineCount++;
                }
                if (codeBegin >= 0 && codeEnd > 0 && codeBegin < codeEnd) {
                    for (int i = codeBegin + 1; i < codeEnd; i++) {
                        code.append(codeList.get(i)).append("\n");
                    }
                } else {
                    Boolean isCode = Boolean.FALSE;
                    for (int i = 0; i < codeList.size(); i++) {
                       String str = codeList.get(i);
                        if (!isCode) {
                            if (StringUtils.isNotBlank(str) && !str.startsWith(codeTypeEnum.getComment())) {
                                isCode = Boolean.TRUE;
                                code.append(str).append("\n");
                            } else {
                                continue;
                            }
                        } else {
                            code.append(str).append("\n");
                        }
                    }
                }
            }
        } catch (IOException id) {

        }
        return code.toString();
    }

    public static String trim(String str) {
        return str.replaceAll("[\\s|\\t]", "");
    }

    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        } else if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (!srcDir.exists()) {
            throw new FileNotFoundException("Source \'" + srcDir + "\' does not exist");
        } else if (!srcDir.isDirectory()) {
            throw new IOException("Source \'" + srcDir + "\' exists but is not a directory");
        } else if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source \'" + srcDir + "\' and destination \'" + destDir + "\' are the same");
        } else {
            doCopyDirectory(srcDir, destDir, preserveFileDate);
        }
    }

    private static void doCopyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination \'" + destDir + "\' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination \'" + destDir + "\' directory cannot be created");
            }

            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }

        if (!destDir.canWrite()) {
            throw new IOException("Destination \'" + destDir + "\' cannot be written to");
        } else {
            File[] files = srcDir.listFiles();
            if (files == null) {
                throw new IOException("Failed to list contents of " + srcDir);
            } else {
                for (int i = 0; i < files.length; ++i) {
                    File copiedFile = new File(destDir, files[i].getName());
                    if (files[i].isDirectory()) {
                        doCopyDirectory(files[i], copiedFile, preserveFileDate);
                    } else {
                        doCopyFile(files[i], copiedFile, preserveFileDate);
                    }
                }

            }
        }
    }

    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination \'" + destFile + "\' exists but is a directory");
        } else {
            FileInputStream input = new FileInputStream(srcFile);

            try {
                FileOutputStream output = new FileOutputStream(destFile);

                try {
                    IOUtils.copy(input, output);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            } finally {
                IOUtils.closeQuietly(input);
            }

            if (srcFile.length() != destFile.length()) {
                throw new IOException("Failed to copy full contents from \'" + srcFile + "\' to \'" + destFile + "\'");
            } else {
                if (preserveFileDate) {
                    destFile.setLastModified(srcFile.lastModified());
                }

            }
        }
    }


}
