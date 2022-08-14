package com.shuzijun.leetcode.plugin.window.navigator;


import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
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
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class SimpleNavigatorPanel extends NavigatorPanel implements Disposable {


    private final NavigatorAction<QuestionView> myNavigatorAction;
    private final Map<String, Find> findMap = new HashMap<>();
    private final JPanel queryPanel;
    private final JTextField queryField;
    private final SimpleNavigatorTable navigatorTable;
    private final ActionToolbar findToolbar;
    private final ActionToolbar actionSortToolbar;
    private final RepositoryService repositoryService;

    public SimpleNavigatorPanel(RepositoryService repositoryService) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.repositoryService = repositoryService;
        this.myNavigatorAction = createMyNavigatorAction();
        navigatorTable = new SimpleNavigatorTable(repositoryService.getProject(), myNavigatorAction);
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

        findToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_FIND_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_FIND_TOOLBAR), true);
        findToolbar.setTargetComponent(navigatorTable);
        actionSortToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_FIND_SORT_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_FIND_SORT_TOOLBAR), true);
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
                repositoryService.getViewService().loadServiceData(this);
            }

            @Override
            public void findChange(String filterKey, boolean b, Tag tag) {
                if ("categorySlug".equals(filterKey)) {
                    if (b) {
                        navigatorTable.getPageInfo().setCategorySlug(tag.getSlug());
                    } else {
                        navigatorTable.getPageInfo().setCategorySlug("");
                    }
                } else {
                    navigatorTable.getPageInfo().disposeFilters(filterKey, tag.getSlug(), b);
                }
                navigatorTable.getPageInfo().setPageIndex(1);
                repositoryService.getViewService().loadServiceData(this);
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
                repositoryService.getViewService().loadServiceData(this);
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
                repositoryService.getViewService().loadServiceData(this);
            }

            @Override
            public void resetServiceData() {
                getFind().resetFilterData(Constant.FIND_TYPE_LISTS, repositoryService.getFindService().getLists());
                repositoryService.getViewService().loadServiceData(this);
            }

            @Override
            public boolean position(String slug) {
                if (StringUtils.isBlank(slug)) {
                    return true;
                }
                if (selectedRow(slug)) {
                    return true;
                }
                QuestionIndex questionIndex = repositoryService.getQuestionService().getQuestionIndex(slug);
                if (questionIndex == null) {
                    return false;
                }
                getFind().operationType("");
                getFind().clearFilter();
                navigatorTable.getPageInfo().clear();

                navigatorTable.getPageInfo().getFilters().setSearchKeywords("");
                queryField.setText("");

                navigatorTable.getPageInfo().setPageIndex((questionIndex.getIndex() / navigatorTable.getPageInfo().getPageSize()) + 1);
                repositoryService.getViewService().loadServiceData(this, slug);
                return selectedRow(slug);
            }
        };
    }

    @Override
    public void dispose() {
        navigatorTable.dispose();
    }

    @Override
    public NavigatorAction getNavigatorAction() {
        return myNavigatorAction;
    }

    private void initFind() {
        Find cnFind = new Find();
        cnFind.addSort(Constant.SORT_TYPE_TITLE, new Sort(Constant.SORT_TYPE_TITLE, "FRONTEND_ID"));
        cnFind.addSort(Constant.SORT_TYPE_SOLUTION, new Sort(Constant.SORT_TYPE_SOLUTION, "SOLUTION_NUM"));
        cnFind.addSort(Constant.SORT_TYPE_ACCEPTANCE, new Sort(Constant.SORT_TYPE_ACCEPTANCE, "AC_RATE"));
        cnFind.addSort(Constant.SORT_TYPE_DIFFICULTY, new Sort(Constant.SORT_TYPE_DIFFICULTY, "DIFFICULTY"));
        cnFind.addSort(Constant.SORT_TYPE_FREQUENCY, new Sort(Constant.SORT_TYPE_FREQUENCY, "FREQUENCY"));
        findMap.put(URLUtils.leetcodecn, cnFind);
        Find enFind = new Find();
        enFind.addSort(Constant.SORT_TYPE_TITLE, new Sort(Constant.SORT_TYPE_TITLE, "FRONTEND_ID"));
        enFind.addSort(Constant.SORT_TYPE_SOLUTION, new Sort(Constant.SORT_TYPE_SOLUTION, "SOLUTION_NUM"));
        enFind.addSort(Constant.SORT_TYPE_ACCEPTANCE, new Sort(Constant.SORT_TYPE_ACCEPTANCE, "AC_RATE"));
        enFind.addSort(Constant.SORT_TYPE_DIFFICULTY, new Sort(Constant.SORT_TYPE_DIFFICULTY, "DIFFICULTY"));
        enFind.addSort(Constant.SORT_TYPE_FREQUENCY, new Sort(Constant.SORT_TYPE_FREQUENCY, "FREQUENCY"));
        findMap.put(URLUtils.leetcode, enFind);
    }
}
