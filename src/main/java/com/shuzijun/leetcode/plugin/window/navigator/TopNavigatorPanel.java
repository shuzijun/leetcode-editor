package com.shuzijun.leetcode.plugin.window.navigator;


import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.shuzijun.leetcode.extension.NavigatorAction;
import com.shuzijun.leetcode.extension.NavigatorPagePanel;
import com.shuzijun.leetcode.extension.NavigatorPanel;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.*;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class TopNavigatorPanel extends NavigatorPanel implements Disposable {

    private final NavigatorAction<CodeTopQuestionView> myNavigatorAction;
    private final Map<String, Find> findMap = new HashMap<>();
    private final JPanel queryPanel;
    private final TopNavigatorTable topNavigatorTable;
    private final ActionToolbar findToolbar;
    private final ActionToolbar actionSortToolbar;
    private final RepositoryService repositoryService;

    public TopNavigatorPanel(RepositoryService repositoryService) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.repositoryService = repositoryService;
        final ActionManager actionManager = ActionManager.getInstance();

        this.myNavigatorAction = createMyNavigatorAction();

        topNavigatorTable = new TopNavigatorTable(repositoryService.getProject(), myNavigatorAction);
        Disposer.register(this, topNavigatorTable);

        ActionToolbar actionToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_CODETOP_NAVIGATOR_ACTIONS_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_CODETOP_NAVIGATOR_ACTIONS_TOOLBAR), true);
        actionToolbar.setTargetComponent(topNavigatorTable);
        setToolbar(actionToolbar.getComponent());

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(Boolean.TRUE, Boolean.TRUE);

        toolWindowPanel.setContent(topNavigatorTable);

        queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));

        findToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_CODETOP_FIND_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_CODETOP_FIND_TOOLBAR), true);
        findToolbar.setTargetComponent(topNavigatorTable);
        actionSortToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_CODETOP_FIND_SORT_TOOLBAR, (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_CODETOP_FIND_SORT_TOOLBAR), true);
        actionSortToolbar.setTargetComponent(topNavigatorTable);
        queryPanel.add(findToolbar.getComponent());
        queryPanel.add(actionSortToolbar.getComponent());

        queryPanel.setVisible(false);
        toolWindowPanel.setToolbar(queryPanel);
        setContent(toolWindowPanel);
        initFind();
        subscribe();

    }

    private void subscribe() {
        repositoryService.subscribeNotifier(Topic.AllQuestionNotifier, myNavigatorAction::resetServiceData, this);
        repositoryService.subscribeNotifier(Topic.ConfigNotifier, (oldConfig, newConfig) -> {
            if (oldConfig != null && !oldConfig.getUrl().equalsIgnoreCase(newConfig.getUrl())) {
                myNavigatorAction.resetServiceData();
            }
        }, this);
    }

    private NavigatorAction<CodeTopQuestionView> createMyNavigatorAction() {
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
                return topNavigatorTable.selectedRow(slug);
            }

            @Override
            public void findClear() {
                topNavigatorTable.getPageInfo().clearFilter();
                getFind().clearFilter();
                repositoryService.getCodeTopService().loadServiceData(this);
            }

            @Override
            public void findChange(String filterKey, boolean b, Tag tag) {
                if ("tags".equalsIgnoreCase(filterKey) && b) {
                    if (topNavigatorTable.getPageInfo().getFilters().getTags() != null) {
                        topNavigatorTable.getPageInfo().getFilters().getTags().clear();
                    }

                }
                topNavigatorTable.getPageInfo().disposeFilters(filterKey, tag.getSlug(), b);
                topNavigatorTable.getPageInfo().setPageIndex(1);
                repositoryService.getCodeTopService().loadServiceData(this);
            }

            @Override
            public Find getFind() {
                return findMap.get(URLUtils.getLeetcodeHost());
            }

            @Override
            public void sort(Sort sort) {
                if (sort.getType() == 0) {
                    topNavigatorTable.getPageInfo().disposeFilters("orderBy", "", false);
                    topNavigatorTable.getPageInfo().disposeFilters("sortOrder", "", false);
                } else if (sort.getType() == 1) {
                    topNavigatorTable.getPageInfo().disposeFilters("orderBy", sort.getSlug(), true);
                    topNavigatorTable.getPageInfo().disposeFilters("sortOrder", "DESCENDING", true);
                } else if (sort.getType() == 2) {
                    topNavigatorTable.getPageInfo().disposeFilters("orderBy", sort.getSlug(), true);
                    topNavigatorTable.getPageInfo().disposeFilters("sortOrder", "ASCENDING", true);
                }
                repositoryService.getCodeTopService().loadServiceData(this);
            }

            @Override
            public CodeTopQuestionView getSelectedRowData() {
                return topNavigatorTable.getSelectedRowData();
            }

            @Override
            public NavigatorPagePanel getPagePanel() {
                return topNavigatorTable.getPagePanel();
            }

            @Override
            public PageInfo<CodeTopQuestionView> getPageInfo() {
                return topNavigatorTable.getPageInfo();
            }

            @Override
            public void loadData(String slug) {
                topNavigatorTable.refreshData(slug);
            }

            @Override
            public void loadServiceData() {
                repositoryService.getCodeTopService().loadServiceData(this);
            }

            @Override
            public void resetServiceData() {
                if (topNavigatorTable.getPageInfo().getRowTotal() > 0) {
                    repositoryService.getCodeTopService().loadServiceData(this);
                }
            }
        };
    }

    @Override
    public Object getData(@NotNull String dataId) {
        return super.getData(dataId);
    }

    private void initFind() {
        Find cnFind = new Find();
        cnFind.addSort(Constant.CODETOP_SORT_TYPE_TITLE, new Sort(Constant.CODETOP_SORT_TYPE_TITLE, "leetcode"));
        cnFind.addSort(Constant.CODETOP_SORT_TYPE_TIME, new Sort(Constant.CODETOP_SORT_TYPE_TIME, "time"));
        cnFind.addSort(Constant.CODETOP_SORT_TYPE_FREQUENCY, new Sort(Constant.CODETOP_SORT_TYPE_FREQUENCY, "frequency"));
        findMap.put(URLUtils.leetcodecn, cnFind);
        Find enFind = new Find();
        enFind.addSort(Constant.CODETOP_SORT_TYPE_TITLE, new Sort(Constant.CODETOP_SORT_TYPE_TITLE, "leetcode"));
        enFind.addSort(Constant.CODETOP_SORT_TYPE_TIME, new Sort(Constant.CODETOP_SORT_TYPE_TIME, "time"));
        enFind.addSort(Constant.CODETOP_SORT_TYPE_FREQUENCY, new Sort(Constant.CODETOP_SORT_TYPE_FREQUENCY, "frequency"));
        findMap.put(URLUtils.leetcode, enFind);
    }

    @Override
    public NavigatorAction<CodeTopQuestionView> getNavigatorAction() {
        return myNavigatorAction;
    }

    public void dispose() {
        topNavigatorTable.dispose();
    }
}
