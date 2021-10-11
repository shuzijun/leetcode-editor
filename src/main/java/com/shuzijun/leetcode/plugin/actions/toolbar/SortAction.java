package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Sort;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import icons.LeetCodeEditorIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class SortAction extends AbstractAction {

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Sort sort = getSort(e);
        if (sort == null) {
            return;
        }
        ActionToolbar sortToolbar = e.getDataContext().getData(DataKeys.LEETCODE_TOOLBAR_SORT);
        if (sort.getType() == Constant.SORT_ASC) {
            e.getPresentation().setIcon(LeetCodeEditorIcons.SORT_ASC);
        } else if (sort.getType() == Constant.SORT_DESC) {
            e.getPresentation().setIcon(LeetCodeEditorIcons.SORT_DESC);
        } else {
            e.getPresentation().setIcon(null);
        }
        if (sortToolbar != null && sortToolbar.getComponent() != null) {
            sortToolbar.getComponent().updateUI();
        }
        super.update(e);

    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        NavigatorTable navigatorTable = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (navigatorTable == null || ViewManager.getFilter(Constant.FIND_TYPE_DIFFICULTY) == null) {
            return;
        }
        Sort sort = getSort(anActionEvent);
        if (sort == null) {
            return;
        }
        ViewManager.operationType(getKey(anActionEvent));
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
        ViewManager.loadServiceData(navigatorTable, anActionEvent.getProject());
    }

    private Sort getSort(AnActionEvent anActionEvent) {
        return ViewManager.getSort(getKey(anActionEvent));
    }

    private String getKey(AnActionEvent anActionEvent) {
        return anActionEvent.getActionManager().getId(this).replace(PluginConstant.LEETCODE_SORT_PREFIX, "");
    }
}
