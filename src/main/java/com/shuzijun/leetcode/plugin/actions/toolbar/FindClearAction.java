package com.shuzijun.leetcode.plugin.actions.toolbar;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

/**
 * @author shuzijun
 */
public class FindClearAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        NavigatorTable navigatorTable = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (navigatorTable == null) {
            return;
        } else {
            navigatorTable.getPageInfo().clearFilter();
        }
        ViewManager.loadServiceData(navigatorTable,anActionEvent.getProject());
    }
}
