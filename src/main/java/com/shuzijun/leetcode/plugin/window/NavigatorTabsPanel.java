package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.User;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.navigator.AllNavigatorPanel;
import com.shuzijun.leetcode.plugin.window.navigator.NavigatorPanel;
import com.shuzijun.leetcode.plugin.window.navigator.TopNavigatorPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shuzijun
 */
public class NavigatorTabsPanel extends SimpleToolWindowPanel implements Disposable {

    private static final DisposableMap<String, NavigatorTabsPanel> NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP = new DisposableMap<>();

    static {
        Disposer.register(ApplicationManager.getApplication(), NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP);
    }

    private String id = UUID.randomUUID().toString();

    private SimpleToolWindowPanel[] navigatorPanels;
    private TabInfo[] tabInfos;

    private JBTabsImpl tabs;

    private int toggleIndex = 0;

    private volatile Map<String, User> userCache = new ConcurrentHashMap<>();

    public NavigatorTabsPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);

        navigatorPanels = new SimpleToolWindowPanel[3];
        tabInfos = new TabInfo[3];

        tabs = new JBTabsImpl(project);
        tabs.setHideTabs(true);

        NavigatorPanel navigatorPanel = new NavigatorPanel(toolWindow, project);
        navigatorPanels[0] = navigatorPanel;

        TabInfo tabInfo = new TabInfo(navigatorPanel);
        tabInfo.setText("page");
        tabInfos[0] = tabInfo;
        tabs.addTab(tabInfo);

        AllNavigatorPanel allNavigatorPanel = new AllNavigatorPanel(toolWindow, project);
        navigatorPanels[1] = allNavigatorPanel;

        TabInfo allTabInfo = new TabInfo(allNavigatorPanel);
        allTabInfo.setText("all");
        tabInfos[1] = allTabInfo;
        tabs.addTab(allTabInfo);

        TopNavigatorPanel topNavigatorPanel = new TopNavigatorPanel(toolWindow, project);
        navigatorPanels[2] = topNavigatorPanel;

        TabInfo topTabInfo = new TabInfo(topNavigatorPanel);
        topTabInfo.setText("codeTop");
        tabInfos[2] = topTabInfo;
        tabs.addTab(topTabInfo);

        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            for (int i = 0; i < tabInfos.length; i++) {
                if (tabInfos[i].getText().equalsIgnoreCase(config.getNavigatorName())) {
                    tabs.select(tabInfos[i], true);
                    toggleIndex = i;
                    break;
                }
            }
        }

        setContent(tabs);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            User user = getUser();
            if (user.isSignedIn()) {
                WindowFactory.updateTitle(project, user.getUsername());
                StatisticsData.refresh(project);
            } else {
                WindowFactory.updateTitle(project, "No login");
            }
        });
        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        messageBusConnection.subscribe(LoginNotifier.TOPIC, new LoginNotifier() {
            @Override
            public void login(Project notifierProject, String host) {
                User user = getUser();
                if (user.isSignedIn()) {
                    WindowFactory.updateTitle(project, user.getUsername());
                    StatisticsData.refresh(project);
                } else {
                    WindowFactory.updateTitle(project, "No login");
                }

            }

            @Override
            public void logout(Project notifierProject, String host) {
                WindowFactory.updateTitle(project, "No login");
            }
        });
        messageBusConnection.subscribe(ConfigNotifier.TOPIC, new ConfigNotifier() {
            @Override
            public void change(Config oldConfig, Config newConfig) {
                if (oldConfig != null && !oldConfig.getUrl().equalsIgnoreCase(newConfig.getUrl())) {
                    User user = getUser();
                    if (user.isSignedIn()) {
                        WindowFactory.updateTitle(project, user.getUsername());
                        StatisticsData.refresh(project);
                    } else {
                        WindowFactory.updateTitle(project, "No login");
                    }
                }
            }
        });
        messageBusConnection.subscribe(QuestionStatusNotifier.QUESTION_STATUS_TOPIC, (QuestionStatusNotifier) question -> StatisticsData.refresh(project));

        for (SimpleToolWindowPanel n : navigatorPanels) {
            if (n != null && navigatorPanel instanceof Disposable) {
                Disposer.register(this, (Disposable) n);
            }
        }

        NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.put(id, this);

    }

    public void toggle() {
        toggleIndex = (toggleIndex + 1) % 3;
        tabs.select(tabInfos[toggleIndex], true);
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            config.setNavigatorName(tabInfos[toggleIndex].getText());
            PersistentConfig.getInstance().setInitConfig(config);
        }
    }

    @NotNull
    public User getUser() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            return new User();
        } else if (userCache.containsKey(config.getUrl())) {
            return userCache.get(config.getUrl());
        } else {
            String otherKey = NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.getOtherKey(id);
            if (otherKey == null || !((NavigatorTabsPanel) NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.get(otherKey)).userCache.containsKey(config.getUrl())) {
                User user = QuestionManager.getUser();
                userCache.put(config.getUrl(), user);
                return user;
            } else {
                User user = ((NavigatorTabsPanel) NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.get(otherKey)).userCache.get(config.getUrl());
                userCache.put(config.getUrl(), user);
                return user;
            }
        }
    }

    @Override
    public Object getData(String dataId) {
        for (SimpleToolWindowPanel navigatorPanel : navigatorPanels) {
            Object object = navigatorPanel.getData(dataId);
            if (object != null) {
                return object;
            }
        }
        if (DataKeys.LEETCODE_PROJECTS_TABS.is(dataId)) {
            return this;
        }
        if (DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION.is(dataId)) {
            SimpleToolWindowPanel panel = navigatorPanels[toggleIndex];
            if (panel instanceof NavigatorPanelAction) {
                return ((NavigatorPanelAction) panel).getNavigatorAction();
            }
        }

        return super.getData(dataId);
    }

    @Override
    public void dispose() {
        NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.remove(id);
        for (SimpleToolWindowPanel navigatorPanel : navigatorPanels) {
            if (navigatorPanel != null && navigatorPanel instanceof Disposable) {
                ((Disposable) navigatorPanel).dispose();
            }
        }
    }

    public static synchronized void loadUser(boolean login) {
        User user = null;
        if (login) {
            for (int i = 0; i <= 50; i++) {
                user = QuestionManager.getUser();
                if (!user.isSignedIn()) {
                    try {
                        Thread.sleep(500 + (i / 10 * 100));
                    } catch (InterruptedException ignore) {
                    }
                } else {
                    break;
                }
                if(i == 50){
                    LogUtils.LOG.warn("User data is not synchronized");
                }
            }
        } else {
            user = new User();
        }
        Collection<NavigatorTabsPanel> collection = NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.values();
        for (NavigatorTabsPanel navigatorTabsPanel : collection) {
            navigatorTabsPanel.userCache.put(URLUtils.getLeetcodeHost(), user);
        }
    }

    public static class DisposableMap<K, V> extends HashMap implements Disposable {
        @Override
        public synchronized Object put(Object key, Object value) {
            return super.put(key,value);
        }

        public synchronized K getOtherKey(K key){
            K otherKey = null;
            for (Object k : this.keySet()) {
                if (!k.equals(key)) {
                    otherKey = key;
                    break;
                }
            }
            return otherKey;
        }

        @Override
        public void dispose() {
            for (Object value : values()) {
                if (value instanceof Disposable) {
                    ((Disposable) value).dispose();
                }
            }
        }
    }
}
