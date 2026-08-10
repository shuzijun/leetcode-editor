package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.JBTabsFactory;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.plugin.application.CacheInvalidationCoordinator;
import com.shuzijun.leetcode.plugin.application.CacheInvalidationReason;
import com.shuzijun.leetcode.plugin.application.LeetCodeApplicationService;
import com.shuzijun.leetcode.plugin.application.LoginGenerationTracker;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.setting.ConfigurationChangeDetector;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.spi.NavigatorContribution;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shuzijun
 */
public class NavigatorTabsPanel extends SimpleToolWindowPanel implements Disposable {

    private static final DisposableMap<String, NavigatorTabsPanel> NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP = new DisposableMap<>();

    static {
        Disposer.register(ApplicationManager.getApplication(), NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP);
    }

    private final String id = UUID.randomUUID().toString();

    private final LinkedList<SimpleToolWindowPanel> navigatorPanels = new LinkedList<>();
    private final LinkedList<TabInfo> tabInfos = new LinkedList<>();

    private final JBTabs tabs;

    private int toggleIndex = 0;

    private volatile Map<String, User> userCache = new ConcurrentHashMap<>();
    private final AtomicBoolean disposed = new AtomicBoolean();
    private final AtomicBoolean userRefreshInFlight = new AtomicBoolean();

    public NavigatorTabsPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);

        tabs = JBTabsFactory.createTabs(project, this);
        tabs.getPresentation().setHideTabs(true);

        List<NavigatorContribution> contributions = LeetCodeApplicationService.getInstance().navigators();
        if (contributions.isEmpty()) {
            throw new IllegalStateException("No navigator contributions are registered");
        }
        for (NavigatorContribution contribution : contributions) {
            SimpleToolWindowPanel navigatorPanel = contribution.createPanel(toolWindow, project);
            navigatorPanels.add(navigatorPanel);

            TabInfo tabInfo = new TabInfo(navigatorPanel);
            tabInfo.setText(contribution.getId());
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

        setContent(tabs.getComponent());

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            User user = getUser();
            if (user.isSignedIn()) {
                WindowFactory.updateTitle(
                        project,
                        ProductServices.navigatorSessionStrategy().displayName(user)
                );
                StatisticsData.refresh(project);
            } else {
                WindowFactory.updateTitle(project, "No login");
            }
        });
        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        messageBusConnection.subscribe(LoginNotifier.TOPIC, new LoginNotifier() {
            @Override
            public void login(Project notifierProject, String host) {
                refreshUser(project);
                ProductServices.navigatorSessionStrategy().afterLogin(project);
            }

            @Override
            public void logout(Project notifierProject, String host) {
                userCache.put(host, new User());
                WindowFactory.updateTitle(project, "No login");
            }
        });
        messageBusConnection.subscribe(ConfigNotifier.TOPIC, new ConfigNotifier() {
            @Override
            public void change(Config oldConfig, Config newConfig) {
                if (oldConfig == null || newConfig == null) {
                    return;
                }
                boolean siteChanged = !Objects.equals(oldConfig.getUrl(), newConfig.getUrl());
                boolean accountChanged = !Objects.equals(oldConfig.getLoginName(), newConfig.getLoginName());
                if (siteChanged || accountChanged) {
                    LoginGenerationTracker.next();
                }
                if (siteChanged) {
                    CacheInvalidationCoordinator.invalidateSiteChange(
                            oldConfig.getUrl(),
                            endpoint(oldConfig.getUrl()),
                            newConfig.getUrl(),
                            endpoint(newConfig.getUrl())
                    );
                    userCache.remove(newConfig.getUrl());
                    refreshUser(project);
                } else if (accountChanged) {
                    CacheInvalidationCoordinator.invalidate(
                            CacheInvalidationReason.ACCOUNT_CHANGE,
                            newConfig.getUrl(),
                            endpoint(newConfig.getUrl())
                    );
                    userCache.remove(newConfig.getUrl());
                    refreshUser(project);
                }
                if (ConfigurationChangeDetector.languageChanged(oldConfig, newConfig)) {
                    CacheInvalidationCoordinator.invalidate(
                            CacheInvalidationReason.LANGUAGE_CHANGE,
                            newConfig.getUrl(),
                            endpoint(newConfig.getUrl())
                    );
                }
                if (ConfigurationChangeDetector.templateChanged(oldConfig, newConfig)) {
                    CacheInvalidationCoordinator.invalidate(
                            CacheInvalidationReason.TEMPLATE_CHANGE,
                            newConfig.getUrl(),
                            endpoint(newConfig.getUrl())
                    );
                }
            }
        });
        messageBusConnection.subscribe(QuestionStatusNotifier.QUESTION_STATUS_TOPIC, question -> StatisticsData.refresh(project));

        for (SimpleToolWindowPanel n : navigatorPanels) {
            if (n instanceof Disposable) {
                Disposer.register(this, (Disposable) n);
            }
        }

        NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.put(id, this);

    }

    public void toggle() {
        toggleIndex = (toggleIndex + 1) % navigatorPanels.size();
        tabs.select(tabInfos.get(toggleIndex), true);
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            config.setNavigatorName(tabInfos.get(toggleIndex).getText());
            PersistentConfig.getInstance().setInitConfig(config);
        }
    }

    public @NotNull User getUser() {
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

    public void refreshUser(Project project) {
        if (!userRefreshInFlight.compareAndSet(false, true)) {
            return;
        }
        AppExecutorUtil.getAppExecutorService().execute(() -> {
            try {
                if (project.isDisposed() || disposed.get()) {
                    return;
                }
                User user = getUser();
                if (project.isDisposed() || disposed.get()) {
                    return;
                }
                if (user.isSignedIn()) {
                    WindowFactory.updateTitle(
                            project,
                            ProductServices.navigatorSessionStrategy().displayName(user)
                    );
                    StatisticsData.refresh(project);
                } else {
                    WindowFactory.updateTitle(project, "No login");
                }
            } finally {
                userRefreshInFlight.set(false);
            }
        });
    }

    @Override
    public Object getData(@NotNull String dataId) {
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
            SimpleToolWindowPanel panel = navigatorPanels.get(toggleIndex);
            if (panel instanceof NavigatorPanelAction) {
                return ((NavigatorPanelAction) panel).getNavigatorAction();
            }
        }

        return super.getData(dataId);
    }

    public NavigatorAction getNavigatorAction() {
        SimpleToolWindowPanel panel = navigatorPanels.get(toggleIndex);
        if (panel instanceof NavigatorPanelAction) {
            return ((NavigatorPanelAction) panel).getNavigatorAction();
        }
        return null;
    }

    @Override
    public void dispose() {
        disposed.set(true);
        NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.remove(id);
        for (SimpleToolWindowPanel navigatorPanel : navigatorPanels) {
            if (navigatorPanel instanceof Disposable) {
                ((Disposable) navigatorPanel).dispose();
            }
        }
    }

    public static CompletableFuture<User> loadUser(boolean login) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            return CompletableFuture.completedFuture(new User());
        }
        String host = config.getUrl();
        if (!login) {
            for (NavigatorTabsPanel navigatorTabsPanel : NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.values()) {
                navigatorTabsPanel.userCache.put(host, new User());
            }
            return CompletableFuture.completedFuture(new User());
        }
        return CompletableFuture.supplyAsync(() -> {
            User user = QuestionManager.getUser();
            for (NavigatorTabsPanel navigatorTabsPanel : NAVIGATOR_TABS_PANEL_DISPOSABLE_MAP.values()) {
                if (user != null && user.isSignedIn()) {
                    navigatorTabsPanel.userCache.put(host, user);
                } else {
                    navigatorTabsPanel.userCache.remove(host);
                }
            }
            if (user == null || !user.isSignedIn()) {
                LogUtils.LOG.warn("User data is not synchronized after login");
            }
            return user;
        }, AppExecutorUtil.getAppExecutorService());
    }

    private static String endpoint(String host) {
        return "https://" + host;
    }

    public static class DisposableMap<K, V> extends ConcurrentHashMap<K, V> implements Disposable {
        public K getOtherKey(K key) {
            for (K candidate : keySet()) {
                if (!candidate.equals(key)) {
                    return candidate;
                }
            }
            return null;
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
