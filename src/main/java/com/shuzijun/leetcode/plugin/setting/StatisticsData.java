package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.shuzijun.leetcode.plugin.manager.SessionManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Session;
import com.shuzijun.leetcode.plugin.model.Statistics;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shuzijun
 */
@State(name = "LeetcodeEditorStatistics" + PluginConstant.ACTION_SUFFIX, storages = {@Storage(value = PluginConstant.ACTION_PREFIX + "/statistics.xml")}, externalStorageOnly = true)
public class StatisticsData implements PersistentStateComponent<StatisticsData.InnerState> {

    @Nullable
    public static StatisticsData getInstance(Project project) {
        return project.getService(StatisticsData.class);
    }

    private InnerState innerState = new InnerState();

    @Nullable
    @Override
    public StatisticsData.InnerState getState() {
        return innerState;
    }

    @Override
    public void loadState(@NotNull StatisticsData.InnerState innerState) {
        this.innerState = innerState;
    }

    public void refresh(String host, Statistics statistics) {
        this.innerState.statistics.put(host, statistics);
    }

    public static void refresh(Project project) {
        StatisticsData statisticsData = StatisticsData.getInstance(project);
        List<Session> sessionList = SessionManager.getSession(project, true);
        if (CollectionUtils.isEmpty(sessionList)) {
            return;
        }
        Session session = sessionList.get(0);
        Statistics statistics = new Statistics();
        statistics.setQuestionTotal(session.getQuestionTotal());
        statistics.setSolvedTotal(session.getSolvedTotal());
        statistics.setEasy(session.getEasy());
        statistics.setMedium(session.getMedium());
        statistics.setHard(session.getHard());
        statisticsData.refresh(URLUtils.getLeetcodeHost(), statistics);
    }


    public static class InnerState {
        @NotNull
        @MapAnnotation
        public Map<String, Statistics> statistics;

        InnerState() {
            statistics = new HashMap<>();
        }
    }

}
