package com.shuzijun.leetcode.plugin.window.navigator;


import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBTextField;
import com.shuzijun.leetcode.extension.NavigatorAction;
import com.shuzijun.leetcode.extension.NavigatorPagePanel;
import com.shuzijun.leetcode.extension.NavigatorPanel;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.*;
import com.shuzijun.leetcode.platform.notifier.ConfigNotifier;
import com.shuzijun.leetcode.platform.notifier.LoginNotifier;
import com.shuzijun.leetcode.plugin.listener.QueryKeyListener;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.PluginTopic;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class AllNavigatorPanel extends NavigatorPanel implements Disposable {


    private final NavigatorAction<QuestionView> myNavigatorAction;
    private final Map<String, Find> findMap = new HashMap<>();
    private final JPanel queryPanel;
    private final JTextField queryField;
    private final AllNavigatorTable navigatorTable;
    private final ActionToolbar findToolbar;
    private final ActionToolbar actionSortToolbar;
    private final RepositoryService repositoryService;

    public AllNavigatorPanel(RepositoryService repositoryService) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.repositoryService = repositoryService;
        this.myNavigatorAction = createMyNavigatorAction();
        navigatorTable = new AllNavigatorTable(repositoryService.getProject(), myNavigatorAction);
        Disposer.register(this, navigatorTable);

        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar(PluginConstant.ACTION_PREFIX + " Toolbar", (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_NAVIGATOR_ACTIONS_TOOLBAR), true);
        actionToolbar.setTargetComponent(navigatorTable);
        setToolbar(actionToolbar.getComponent());

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(Boolean.TRUE, Boolean.TRUE);

        toolWindowPanel.setContent(navigatorTable);

        queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
        queryField = new JBTextField();
        queryField.setToolTipText("Enter Search");
        queryField.addKeyListener(new QueryKeyListener(queryField, myNavigatorAction, repositoryService.getProject()));
        queryPanel.add(queryField);

        findToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_ALL_FIND_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_ALL_FIND_TOOLBAR), true);
        findToolbar.setTargetComponent(navigatorTable);
        actionSortToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_ALL_FIND_SORT_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_ALL_FIND_SORT_TOOLBAR), true);
        actionSortToolbar.setTargetComponent(navigatorTable);
        queryPanel.add(findToolbar.getComponent());
        queryPanel.add(actionSortToolbar.getComponent());

        queryPanel.setVisible(false);
        toolWindowPanel.setToolbar(queryPanel);
        setContent(toolWindowPanel);
        initFind();
        subscribe();

    }

    private void subscribe() {

        repositoryService.subscribeNotifier(Topic.LoginNotifier, new LoginNotifier() {
            @Override
            public void login(Project project, String host) {
                ProgressManager.getInstance().run(new Task.Backgroundable(repositoryService.getProject(), "Refresh data", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        if (!project.equals(myProject) && myNavigatorAction.getPageInfo().getRowTotal() <= 0) {
                            return;
                        }
                        myNavigatorAction.resetServiceData();
                        ApplicationManager.getApplication().getMessageBus().syncPublisher(PluginTopic.ALL_QUESTION_TOPIC).reset();
                    }
                });
            }

            @Override
            public void logout(Project project, String host) {
                ProgressManager.getInstance().run(new Task.Backgroundable(repositoryService.getProject(), "Refresh data", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        if (!project.equals(myProject) && myNavigatorAction.getPageInfo().getRowTotal() <= 0) {
                            return;
                        }
                        myNavigatorAction.resetServiceData();
                        ApplicationManager.getApplication().getMessageBus().syncPublisher(PluginTopic.ALL_QUESTION_TOPIC).reset();
                    }
                });
            }
        }, this);
        repositoryService.subscribeNotifier(Topic.ConfigNotifier, new ConfigNotifier() {
            @Override
            public void change(Config oldConfig, Config newConfig) {
                if (oldConfig != null && !oldConfig.getUrl().equalsIgnoreCase(newConfig.getUrl())) {
                    if (navigatorTable.getPageInfo().getRowTotal() > 0) {
                        myNavigatorAction.loadServiceData();
                    }
                }
            }
        }, this);
    }

    private NavigatorAction<QuestionView> createMyNavigatorAction() {
        return new NavigatorAction.Adapter<>() {
            @Override
            public void updateUI() {
                findToolbar.getComponent().updateUI();
                actionSortToolbar.getComponent().updateUI();
            }

            @Override
            public JPanel queryPanel() {
                return queryPanel;
            }

            @Override
            public boolean selectedRow(String slug) {
                return navigatorTable.selectedRow(slug);
            }

            @Override
            public Find getFind() {
                return findMap.get(URLUtils.getLeetcodeHost());
            }

            @Override
            public void findClear() {
                navigatorTable.getPageInfo().clearFilter();
                getFind().clearFilter();
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public void findChange(String filterKey, boolean b, Tag tag) {
                if ("categorySlug".equals(filterKey)) {
                    if (b) {
                        navigatorTable.getPageInfo().setCategorySlug(tag.getName());
                    } else {
                        navigatorTable.getPageInfo().setCategorySlug("");
                    }
                } else {
                    navigatorTable.getPageInfo().disposeFilters(filterKey, tag.getSlug(), b);
                }
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public void sort(Sort sort) {
                if (sort.getType() == 0) {
                    navigatorTable.getPageInfo().disposeFilters("orderBy", "", false);
                    navigatorTable.getPageInfo().disposeFilters("sortOrder", "", false);
                } else if (sort.getType() == 1) {
                    navigatorTable.getPageInfo().disposeFilters("orderBy", sort.getSlug(), true);
                    navigatorTable.getPageInfo().disposeFilters("sortOrder", "DESCENDING", true);
                } else if (sort.getType() == 2) {
                    navigatorTable.getPageInfo().disposeFilters("orderBy", sort.getSlug(), true);
                    navigatorTable.getPageInfo().disposeFilters("sortOrder", "ASCENDING", true);
                }
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public QuestionView getSelectedRowData() {
                return navigatorTable.getSelectedRowData();
            }

            @Override
            public NavigatorPagePanel getPagePanel() {
                return navigatorTable.getPagePanel();
            }

            @Override
            public PageInfo<QuestionView> getPageInfo() {
                return navigatorTable.getPageInfo();
            }

            @Override
            public void loadData(String slug) {
                navigatorTable.refreshData(slug);
            }

            @Override
            public void loadServiceData() {
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public void resetServiceData() {
                getFind().resetFilterData(Constant.FIND_TYPE_LISTS, repositoryService.getFindService().getLists());
                repositoryService.getViewService().loadAllServiceData(this, null, true);
            }

            @Override
            public boolean position(String slug) {
                if (StringUtils.isBlank(slug)) {
                    return true;
                }
                if (selectedRow(slug)) {
                    return true;
                }
                navigatorTable.getPageInfo().clearFilter();
                navigatorTable.getPageInfo().getFilters().setSearchKeywords("");
                queryField.setText("");

                getFind().clearFilter();
                repositoryService.getViewService().loadAllServiceData(this, slug, false);
                return selectedRow(slug);
            }
        };
    }

    @Override
    public Object getData(@NotNull String dataId) {
        return super.getData(dataId);
    }

    @Override
    public void dispose() {
        navigatorTable.dispose();
    }

    @Override
    public NavigatorAction<QuestionView> getNavigatorAction() {
        return myNavigatorAction;
    }

    private void initFind() {
        Find cnFind = new Find();
        cnFind.addSort(Constant.SORT_TYPE_TITLE, new Sort(Constant.SORT_TYPE_TITLE, "TITLE"));
        cnFind.addSort(Constant.SORT_TYPE_DIFFICULTY, new Sort(Constant.SORT_TYPE_DIFFICULTY, "DIFFICULTY"));
        cnFind.addSort(Constant.SORT_TYPE_STATES, new Sort(Constant.SORT_TYPE_STATES, "STATES"));
        findMap.put(URLUtils.leetcodecn, cnFind);
        Find enFind = new Find();
        enFind.addSort(Constant.SORT_TYPE_TITLE, new Sort(Constant.SORT_TYPE_TITLE, "TITLE"));
        enFind.addSort(Constant.SORT_TYPE_DIFFICULTY, new Sort(Constant.SORT_TYPE_DIFFICULTY, "DIFFICULTY"));
        enFind.addSort(Constant.SORT_TYPE_STATES, new Sort(Constant.SORT_TYPE_STATES, "STATES"));
        findMap.put(URLUtils.leetcode, enFind);
    }
}
