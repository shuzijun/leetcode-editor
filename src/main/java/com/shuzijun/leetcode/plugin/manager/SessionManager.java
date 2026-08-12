package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.application.LeetCodeSessionService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.lc.model.Session;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author shuzijun
 */
public class SessionManager {

    public static List<Session> getSession(Project project) {
        return getSession(project, false);
    }

    public static List<Session> getSession(Project project, boolean cache) {
        try {
            String userSlug = null;
            if (URLUtils.isCn()) {
                userSlug = WindowFactory.getDataContext(project)
                        .getData(DataKeys.LEETCODE_PROJECTS_TABS)
                        .getUser()
                        .getUserSlug();
            }
            return sessionService().list(userSlug, cache);
        } catch (Exception exception) {
            LogUtils.LOG.error("获取会话进度失败", exception);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            return Collections.emptyList();
        }
    }

    public static boolean switchSession(Project project, Integer id) {
        try {
            if (sessionService().switchTo(id)) {
                return true;
            }
        } catch (Exception exception) {
            LogUtils.LOG.error("切换会话失败", exception);
        }
        MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        return false;
    }

    private static LeetCodeSessionService sessionService() {
        return LeetCodeServices.session();
    }
}
