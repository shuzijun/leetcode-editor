package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.application.LeetCodeSolutionService;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.utils.doc.CleanMarkdown;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author shuzijun
 */
public class ArticleManager {

    private static final int SOLUTION_PAGE_COUNT = 4;
    private static final int SOLUTION_PAGE_SIZE = 30;
    private static final long SOLUTION_LOAD_TIMEOUT_SECONDS = 35;

    public static File openArticle(
            String titleSlug,
            String articleSlug,
            Project project,
            Boolean isOpenEditor
    ) {
        return openArticle(titleSlug, articleSlug, articleSlug, project, isOpenEditor);
    }

    public static File openArticle(
            String titleSlug,
            String articleSlug,
            String articleId,
            Project project,
            Boolean isOpenEditor
    ) {
        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_SOLUTION + articleSlug + "." + PluginConstant.LEETCODE_EDITOR_VIEW;

        File file = new File(filePath);
        String host = URLUtils.getLeetcodeProblems()
                + titleSlug
                + (URLUtils.isCn() ? "/solution/" : "/solutions/")
                + articleSlug
                + "/";
        if (!file.exists()) {
            String article = getArticle(articleId, project);
            if (StringUtils.isBlank(article)) {
                return file;
            }
            article = formatMarkdown(article, host);

            FileUtils.saveFile(file, article);
        } else {
            migrateEscapedMarkdownCache(file, host);
        }
        if (isOpenEditor) {
            FileUtils.openFileEditor(file, project);
        }
        return file;
    }

    private static String getArticle(String articleId, Project project) {
        try {
            String content = solutionService().loadArticle(articleId);
            if (StringUtils.isBlank(content)) {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.auth"));
                return null;
            } else {
                return content;
            }
        } catch (Exception e) {
            LogUtils.LOG.error("article acquisition failed", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return null;
    }


    public static String formatMarkdown(String content, String host) {
        return CleanMarkdown.cleanMarkdown(normalizeEscapedMarkdown(content), host);
    }

    private static void migrateEscapedMarkdownCache(File file, String host) {
        String cached = FileUtils.getFileBody(file);
        String normalized = normalizeEscapedMarkdown(cached);
        if (!StringUtils.equals(cached, normalized)) {
            FileUtils.saveFile(file, CleanMarkdown.cleanMarkdown(normalized, host));
        }
    }

    static String normalizeEscapedMarkdown(String content) {
        if (StringUtils.isEmpty(content)
                || content.indexOf('\n') >= 0
                || content.indexOf('\r') >= 0) {
            return content;
        }

        int escapedNewlines = 0;
        int offset = 0;
        while ((offset = content.indexOf("\\n", offset)) >= 0) {
            escapedNewlines++;
            offset += 2;
        }
        if (escapedNewlines < 3) {
            return content;
        }
        return unescapeMarkdown(content);
    }

    private static String unescapeMarkdown(String content) {
        StringBuilder result = new StringBuilder(content.length());
        for (int i = 0; i < content.length(); i++) {
            char current = content.charAt(i);
            if (current != '\\' || i + 1 >= content.length()) {
                result.append(current);
                continue;
            }
            char escaped = content.charAt(++i);
            switch (escaped) {
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                default:
                    result.append('\\').append(escaped);
                    break;
            }
        }
        return result.toString();
    }

    public static List<Solution> getSolutionList(String titleSlug, Project project) {
        List<Solution> solutionList = new ArrayList<>();
        try {
            CountDownLatch latch = new CountDownLatch(SOLUTION_PAGE_COUNT);
            List<Solution>[] results = new List[SOLUTION_PAGE_COUNT];
            List<Future<?>> requests = new ArrayList<>(SOLUTION_PAGE_COUNT);

            for (int i = 0; i < SOLUTION_PAGE_COUNT; i++) {
                int pageIndex = i;
                int skip = pageIndex * SOLUTION_PAGE_SIZE;
                requests.add(AppExecutorUtil.getAppExecutorService().submit(() -> {
                    try {
                        results[pageIndex] = solutionService().loadPage(
                                titleSlug,
                                SOLUTION_PAGE_SIZE,
                                skip
                        );
                    } catch (Exception exception) {
                        LogUtils.LOG.error("solution page acquisition failed", exception);
                        MessageUtils.getInstance(project).showWarnMsg(
                                "error",
                                PropertiesUtils.getInfo("response.code")
                        );
                    } finally {
                        latch.countDown();
                    }
                }));
            }

            if (!latch.await(SOLUTION_LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LogUtils.LOG.warn("Timed out loading solution list for " + titleSlug);
                for (Future<?> request : requests) {
                    request.cancel(true);
                }
            }
            for (List<Solution> pageResult : results) {
                if (pageResult != null) {
                    solutionList.addAll(pageResult);
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LogUtils.LOG.warn("Interrupted while loading solution list for " + titleSlug, exception);
        } catch (Exception e) {
            LogUtils.LOG.error("solutionList acquisition failed", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return solutionList;

    }

    private static LeetCodeSolutionService solutionService() {
        return LeetCodeServices.solution();
    }
}
