package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;

@State(
        name = "LeetcodeEditorStatistics",
        storages = @Storage("leetcode/statistics.xml"),
        externalStorageOnly = true
)
public final class DefaultStatisticsData extends StatisticsData {
}
