package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.User;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.window.navigator.AllNavigatorPanel;
import com.shuzijun.leetcode.plugin.window.navigator.NavigatorPanel;
import com.shuzijun.leetcode.plugin.window.navigator.TopNavigatorPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shuzijun
 */
public class NavigatorTabsPanel extends SimpleToolWindowPanel implements Disposable {

    private static final Map<String, User> USER_CACHE = new ConcurrentHashMap<>();

    private final SimpleToolWindowPanel[] navigatorPanels;
    private final String[] navigatorNames = {"page", "all", "codeTop"};
    private final JPanel navigatorCards = new JPanel(new CardLayout());
    private final AtomicBoolean disposed = new AtomicBoolean();
    private final AtomicBoolean userRefreshInFlight = new AtomicBoolean();

    private int toggleIndex = 0;

    public NavigatorTabsPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        navigatorPanels = new SimpleToolWindowPanel[3];
        navigatorPanels[0] = new NavigatorPanel(toolWindow, project);
        navigatorPanels[1] = new AllNavigatorPanel(toolWindow, project);
        navigatorPanels[2] = new TopNavigatorPanel(toolWindow, project);
        for (int i = 0; i < navigatorPanels.length; i++) {
            SimpleToolWindowPanel navigatorPanel = navigatorPanels[i];
            navigatorCards.add(navigatorPanel, navigatorNames[i]);
            if (navigatorPanel instanceof Disposable) {
                Disposer.register(this, (Disposable) navigatorPanel);
            }
        }

        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            for (int i = 0; i < navigatorNames.length; i++) {
                if (navigatorNames[i].equalsIgnoreCase(config.getNavigatorName())) {
                    toggleIndex = i;
                    break;
                }
            }
        }

        selectNavigator(toggleIndex);
        setContent(navigatorCards);

        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        messageBusConnection.subscribe(LoginNotifier.TOPIC, new LoginNotifier() {
            @Override
            public void login(Project notifierProject, String host) {
                QuestionManager.invalidateCaches(host);
                refreshUser(project);
            }

            @Override
            public void logout(Project notifierProject, String host) {
                QuestionManager.invalidateCaches(host);
                WindowFactory.updateTitle(project, "No login");
            }
        });
        messageBusConnection.subscribe(ConfigNotifier.TOPIC, new ConfigNotifier() {
            @Override
            public void change(Config oldConfig, Config newConfig) {
                if (oldConfig != null && (!oldConfig.getUrl().equalsIgnoreCase(newConfig.getUrl())
                        || !Objects.equals(oldConfig.getEnglishContent(), newConfig.getEnglishContent()))) {
                    QuestionManager.invalidateCaches(oldConfig.getUrl());
                    QuestionManager.invalidateCaches(newConfig.getUrl());
                    refreshUser(project);
                }
            }
        });
        messageBusConnection.subscribe(QuestionStatusNotifier.QUESTION_STATUS_TOPIC, (QuestionStatusNotifier) question -> StatisticsData.refresh(project));

    }

    public void toggle() {
        toggleIndex = (toggleIndex + 1) % 3;
        selectNavigator(toggleIndex);
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            config.setNavigatorName(navigatorNames[toggleIndex]);
            PersistentConfig.getInstance().setInitConfig(config);
        }
    }

    private void selectNavigator(int index) {
        ((CardLayout) navigatorCards.getLayout()).show(navigatorCards, navigatorNames[index]);
        navigatorCards.revalidate();
        navigatorCards.repaint();
    }

    @NotNull
    public User getUser() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            return new User();
        }
        return USER_CACHE.computeIfAbsent(config.getUrl(), ignored -> QuestionManager.getUser());
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
                    WindowFactory.updateTitle(project, user.getUsername());
                    StatisticsData.refresh(project);
                } else {
                    WindowFactory.updateTitle(project, "No login");
                }
            } finally {
                userRefreshInFlight.set(false);
            }
        });
    }

    public NavigatorAction getNavigatorAction() {
        SimpleToolWindowPanel panel = navigatorPanels[toggleIndex];
        if (panel instanceof NavigatorPanelAction) {
            return ((NavigatorPanelAction) panel).getNavigatorAction();
        }
        return null;
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }

    public static CompletableFuture<User> loadUser(boolean login) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            return CompletableFuture.completedFuture(new User());
        }
        String host = config.getUrl();
        if (!login) {
            User user = new User();
            USER_CACHE.put(host, user);
            return CompletableFuture.completedFuture(user);
        }

        return CompletableFuture.supplyAsync(() -> {
            User user = QuestionManager.getUser();
            if (user != null && user.isSignedIn()) {
                USER_CACHE.put(host, user);
            } else {
                USER_CACHE.remove(host);
                LogUtils.LOG.warn("User data is not synchronized after login");
            }
            return user;
        }, AppExecutorUtil.getAppExecutorService());
    }
}
