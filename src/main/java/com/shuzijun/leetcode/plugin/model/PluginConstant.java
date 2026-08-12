package com.shuzijun.leetcode.plugin.model;

import com.shuzijun.leetcode.plugin.product.ProductProfile;
import com.shuzijun.leetcode.plugin.product.ProductServices;

/**
 * 插件常量
 *
 * @author shuzijun
 */
public class PluginConstant {
    private static final ProductProfile PROFILE = ProductServices.profile();

    public static final String WEB_ID = PROFILE.marketplacePluginId();
    public static final String PLUGIN_ID = PROFILE.pluginId();
    public static final String PLUGIN_NAME = PROFILE.pluginName();

    /**
     * 通知分组
     */
    public static final String NOTIFICATION_GROUP = PROFILE.notificationGroup();
    public static final String TOOL_WINDOW_ID = PROFILE.toolWindowId();
    public static final String CONSOLE_WINDOW_ID = PROFILE.consoleToolWindowId();

    /**
     * 配置id
     */
    public static final String APPLICATION_CONFIGURABLE_ID = PROFILE.configurableId();

    /**
     * 配置名称
     */
    public static final String APPLICATION_CONFIGURABLE_DISPLAY_NAME =
            PROFILE.configurableDisplayName();


    public static final String ACTION_PREFIX = PROFILE.actionPrefix();
    public static final String ACTION_SUFFIX = PROFILE.actionSuffix();

    public static final String LEETCODE_FIND_PREFIX = ACTION_PREFIX + ".find.";
    public static final String LEETCODE_FIND_TAGS = ACTION_PREFIX + ".find.Tags";

    public static final String LEETCODE_SORT_PREFIX = ACTION_PREFIX + ".sort.";

    public static final String LEETCODE_EDITOR_FAVORITE = ACTION_PREFIX + ".editor.favorite";

    public static final String LEETCODE_NAVIGATOR_ACTIONS_MENU = ACTION_PREFIX + ".NavigatorActionsMenu";

    public static final String LEETCODE_EDITOR_OPEN_CODE = ACTION_PREFIX + ".editor.openCode";

    public static final String LEETCODE_EDITOR_TREE = ACTION_PREFIX + ".editor.tree";

    public static final String LEETCODE_TIMER_BAR_WIDGET = ACTION_PREFIX + ".TimerBarWidget";

    public static final String LEETCODE_NAVIGATOR_ACTIONS_TOOLBAR = ACTION_PREFIX + ".NavigatorActionsToolbar";
    public static final String LEETCODE_FIND_TOOLBAR = ACTION_PREFIX + ".find.Toolbar";

    public static final String LEETCODE_FIND_SORT_TOOLBAR = ACTION_PREFIX + ".find.SortToolbar";

    public static final String LEETCODE_EDITOR_GROUP = ACTION_PREFIX + ".editor.group";

    public static final String LEETCODE_EDITOR_NOTE = ACTION_PREFIX + ".editor.note";

    public static final String LEETCODE_EDITOR_TIMER_STATUS_BAR_ID = PLUGIN_ID + "-TimerStatusBar";

    public static final String LEETCODE_EDITOR_VIEW = PROFILE.fileExtension();

    public static final String LEETCODE_EDITOR_TAB_VIEW = PLUGIN_ID + ".editor.tab";

    public static final String LEETCODE_EDITOR_LOGIN_TOPIC = PLUGIN_ID + ".login.topic";

    public static final String LEETCODE_EDITOR_QUESTION_STATUS_TOPIC = PLUGIN_ID + ".question.status";

    public static final String LEETCODE_EDITOR_QUESTION_SUBMIT_TOPIC = PLUGIN_ID + ".question.submit";

    public static final String LEETCODE_EDITOR_LOGIN_CLOSE_TOPIC = PLUGIN_ID + ".login.close";

    public static final String LEETCODE_CODETOP_NAVIGATOR_ACTIONS_TOOLBAR = ACTION_PREFIX + ".codetop.NavigatorActionsToolbar";
    public static final String LEETCODE_CODETOP_FIND_TOOLBAR = ACTION_PREFIX + ".codetop.find.Toolbar";
    public static final String LEETCODE_CODETOP_FIND_SORT_TOOLBAR = ACTION_PREFIX + ".codetop.find.SortToolbar";

    public static final String LEETCODE_CODETOP_FIND_PREFIX = ACTION_PREFIX + ".codetop.find.";
    public static final String LEETCODE_CODETOP_SORT_PREFIX = ACTION_PREFIX + ".codetop.sort.";

    public static final String LEETCODE_EDITOR_CONFIG_TOPIC = PLUGIN_ID + ".config.topic";

    public static final String LEETCODE_ALL_FIND_TOOLBAR = ACTION_PREFIX + ".all.find.Toolbar";
    public static final String LEETCODE_ALL_FIND_SORT_TOOLBAR = ACTION_PREFIX + ".all.find.SortToolbar";

    public static final String LEETCODE_ALL_FIND_PREFIX = ACTION_PREFIX + ".all.find.";
    public static final String LEETCODE_ALL_FIND_TAGS = ACTION_PREFIX + ".all.find.Tags";
    public static final String LEETCODE_ALL_SORT_PREFIX = ACTION_PREFIX + ".all.sort.";

    public static final String LEETCODE_ALL_QUESTION_TOPIC = PLUGIN_ID + ".all.question.topic";


}
