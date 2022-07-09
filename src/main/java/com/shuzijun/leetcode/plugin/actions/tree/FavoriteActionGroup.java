package com.shuzijun.leetcode.plugin.actions.tree;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import java.util.List;

/**
 * @author shuzijun
 */
public class FavoriteActionGroup extends ActionGroup implements DumbAware {

    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {
        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        List<AnAction> anActionList = Lists.newArrayList();
        List<Tag> tags = navigatorAction.getFind().getFilter(Constant.FIND_TYPE_LISTS);
        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                if (!"leetcode_favorites".equals(tag.getType())) {
                    anActionList.add(new FavoriteAction(tag.getName(), tag));
                }
            }
        }
        AnAction[] anActions = new AnAction[anActionList.size()];
        anActionList.toArray(anActions);
        return anActions;

    }

}
