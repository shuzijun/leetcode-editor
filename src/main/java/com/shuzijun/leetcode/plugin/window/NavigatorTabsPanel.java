package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.extension.NavigatorPanel;
import com.shuzijun.leetcode.extension.NavigatorPanelAction;
import com.shuzijun.leetcode.extension.NavigatorPanelFactory;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.User;
import com.shuzijun.leetcode.platform.notifier.ConfigNotifier;
import com.shuzijun.leetcode.platform.notifier.LoginNotifier;
import com.shuzijun.leetcode.platform.utils.LogUtils;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.PluginTopic;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shuzijun
 */
public class NavigatorTabsPanel extends SimpleToolWindowPanel implements Disposable {

    private static final ExtensionPointName<NavigatorPanelFactory> EXTENSION_NAVIGATOR = ExtensionPointName.create(PluginConstant.EXTENSION_NAVIGATOR);

    private static final DisposableMap<String, NavigatorTabsPanel> NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP = new DisposableMap<>();

    static {
        Disposer.register(ApplicationManager.getApplication(), NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP);
    }

    private final String id = UUID.randomUUID().toString();

    private final RepositoryService repositoryService;

    private final LinkedList<NavigatorPanel> navigatorPanels = new LinkedList<>();
    private final LinkedList<TabInfo> tabInfos = new LinkedList<>();

    private final JBTabsImpl tabs;
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private int toggleIndex = 0;

    public NavigatorTabsPanel(ToolWindow ignoredToolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);

        this.repositoryService = RepositoryServiceImpl.getInstance(project);

        tabs = new JBTabsImpl(project);
        tabs.setHideTabs(true);

        List<NavigatorPanelFactory> extensionList = EXTENSION_NAVIGATOR.getExtensionList();
        for (NavigatorPanelFactory factory : extensionList) {
            NavigatorPanel navigatorPanel = factory.createPanel(repositoryService);
            navigatorPanels.add(navigatorPanel);

            TabInfo tabInfo = new TabInfo(navigatorPanel);
            tabInfo.setText(factory.getName());
            tabInfos.add(tabInfo);
            tabs.addTab(tabInfo);
        }

        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            for (int i = 0; i < tabInfos.size(); i++) {
                if (tabInfos.get(i).getText().equalsIgnoreCase(config.getNavigatorName())) {
                    tabs.select(tabInfos.get(i), true);
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
        messageBusConnection.subscribe(PluginTopic.LOGIN_TOPIC, new LoginNotifier() {
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
        messageBusConnection.subscribe(PluginTopic.CONFIG_TOPIC, new ConfigNotifier() {
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
        messageBusConnection.subscribe(PluginTopic.QUESTION_STATUS_TOPIC, question -> StatisticsData.refresh(project));

        for (JPanel n : navigatorPanels) {
            if (n instanceof Disposable) {
                Disposer.register(this, (Disposable) n);
            }
        }

        NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.put(id, this);

    }

    public static synchronized void loadUser(boolean login, Project project) {
        User user = null;
        if (login) {
            for (int i = 0; i <= 50; i++) {
                user = RepositoryServiceImpl.getInstance(project).getQuestionService().getUser();
                if (!user.isSignedIn()) {
                    try {
                        Thread.sleep(500 + (i / 10 * 100));
                    } catch (InterruptedException ignore) {
                    }
                } else {
                    break;
                }
                if (i == 50) {
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

    public void toggle() {
        toggleIndex = (toggleIndex + 1) % 3;
        tabs.select(tabInfos.get(toggleIndex), true);
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            config.setNavigatorName(tabInfos.get(toggleIndex).getText());
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
            String otherKey = null;
            for (String key : NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.keySet()) {
                if (!key.equals(id)) {
                    otherKey = key;
                    break;
                }
            }
            if (otherKey == null || !NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.get(otherKey).userCache.containsKey(config.getUrl())) {
                User user = repositoryService.getQuestionService().getUser();
                userCache.put(config.getUrl(), user);
                return user;
            } else {
                User user = NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.get(otherKey).userCache.get(config.getUrl());
                userCache.put(config.getUrl(), user);
                return user;
            }
        }
    }

    @Override
    public Object getData(@NotNull String dataId) {
        for (JPanel panel : navigatorPanels) {
            if (panel instanceof DataProvider) {
                Object object = ((DataProvider) panel).getData(dataId);
                if (object != null) {
                    return object;
                }
            }
        }
        if (DataKeys.LEETCODE_PROJECTS_TABS.is(dataId)) {
            return this;
        }
        if (DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION.is(dataId)) {
            JPanel panel = navigatorPanels.get(toggleIndex);
            if (panel instanceof NavigatorPanelAction) {
                return ((NavigatorPanelAction) panel).getNavigatorAction();
            }
        }

        return super.getData(dataId);
    }

    @Override
    public void dispose() {
        NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.remove(id);
        for (JPanel panel : navigatorPanels) {
            if (panel instanceof Disposable) {
                ((Disposable) panel).dispose();
            }
        }
    }

    public static class DisposableMap<K, V> extends HashMap<K, V> implements Disposable {
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
