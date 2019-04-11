package com.shuzijun.leetcode.plugin.actions;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Tag;

import java.util.List;

/**
 * @author shuzijun
 */
public class FavoriteActionGroup extends ActionGroup {

    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {

        List<AnAction> anActionList = Lists.newArrayList();
        List<Tag> tags = ViewManager.getFilter(Constant.FIND_TYPE_LISTS);
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
