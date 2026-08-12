package com.shuzijun.leetcode.plugin.manager;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.application.LeetCodeFindService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author shuzijun
 */
public class FindManager {

    public static List<Tag> getDifficulty() {

        List<String> keyList = Lists.newArrayList(Constant.DIFFICULTY_EASY, Constant.DIFFICULTY_MEDIUM, Constant.DIFFICULTY_HARD);
        List<Tag> difficultyList = Lists.newArrayList();
        for (String key : keyList) {
            Tag tag = new Tag();
            tag.setName(key);
            tag.setSlug(key.toUpperCase());
            difficultyList.add(tag);
        }
        return difficultyList;
    }

    public static List<Tag> getStatus() {
        List<String> keyList = Lists.newArrayList(Constant.STATUS_TODO, Constant.STATUS_SOLVED, Constant.STATUS_ATTEMPTED);

        List<Tag> statusList = Lists.newArrayList();
        for (String key : keyList) {
            Tag tag = new Tag();
            tag.setName(key);
            if (Constant.STATUS_TODO.equals(key)) {
                tag.setSlug("NOT_STARTED");
            } else if (Constant.STATUS_SOLVED.equals(key)) {
                tag.setSlug("AC");
            } else if (Constant.STATUS_ATTEMPTED.equals(key)) {
                tag.setSlug("TRIED");
            }
            statusList.add(tag);
        }
        return statusList;
    }

    public static List<Tag> getTags() {
        try {
            return findService().tags();
        } catch (Exception exception) {
            LogUtils.LOG.error("Request tags exception", exception);
            return Collections.emptyList();
        }
    }

    public static List<Tag> getLists(Project project) {
        try {
            String username = WindowFactory.getDataContext(project)
                    .getData(DataKeys.LEETCODE_PROJECTS_TABS)
                    .getUser()
                    .getUsername();
            return findService().lists(username);
        } catch (Exception exception) {
            LogUtils.LOG.error("Request Lists exception", exception);
            return Collections.emptyList();
        }
    }

    public static List<Tag> getCategory() {
        try {
            return findService().categories();
        } catch (Exception exception) {
            LogUtils.LOG.warn("Request CardInfo exception", exception);
            return Collections.emptyList();
        }
    }

    private static LeetCodeFindService findService() {
        return LeetCodeServices.find();
    }
}
