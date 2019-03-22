package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author shuzijun
 */
public class FileUtils {

    private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void saveFile(String path, String body) {
        saveFile(new File(path), body);
    }

    public static void saveFile(File file, String body) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, Boolean.FALSE);
            fileOutputStream.write(body.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (IOException io) {
            logger.error("获取题目翻译错误", io);
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
                logger.error("读取文件错误", i);

            }
        }
        return all;
    }

    public static String getClearCommentFileBody(File file, CodeTypeEnum codeTypeEnum) {

        StringBuffer code = new StringBuffer();
        try {
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(inputReader);
            Boolean isCode = Boolean.FALSE;
            String str;
            while ((str = bf.readLine()) != null) {
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
            bf.close();
            inputReader.close();
        } catch (IOException id) {

        }
        return code.toString();
    }

}
