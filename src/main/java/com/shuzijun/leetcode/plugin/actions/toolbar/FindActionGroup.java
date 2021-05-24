package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import icons.LeetCodeEditorIcons;

import java.util.List;

/**
 * @author shuzijun
 */
public class FindActionGroup extends ActionGroup {

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    @Override
    public void update(AnActionEvent e) {
        ActionToolbar findToolbar = e.getDataContext().getData(DataKeys.LEETCODE_TOOLBAR_FIND);
        String id = e.getActionManager().getId(this);
        List<Tag> tags = getTags(id);

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                if(tag.isSelect()){
                    e.getPresentation().setIcon(LeetCodeEditorIcons.FILTER);
                    findToolbar.getComponent().updateUI();
                    return;
                }
            }
        }
        e.getPresentation().setIcon(null);
        findToolbar.getComponent().updateUI();
    }


    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {

        List<AnAction> anActionList = Lists.newArrayList();

        String id = anActionEvent.getActionManager().getId(this);

        List<Tag> tags = getTags(id);

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                if ("leetcode.find.Category".equals(id)) {
                    anActionList.add(new FindTagAction(tag.getName(), tag, true));
                }else {
                    anActionList.add(new FindTagAction(tag.getName(), tag));
                }
            }
        }
        AnAction[] anActions = new AnAction[anActionList.size()];
        anActionList.toArray(anActions);
        return anActions;
    }

    private List<Tag> getTags(String id) {
        List<Tag> tags = null;
        if ("leetcode.find.Difficulty".equals(id)) {
            tags = ViewManager.getFilter(Constant.FIND_TYPE_DIFFICULTY);
        } else if ("leetcode.find.Status".equals(id)) {
            tags = ViewManager.getFilter(Constant.FIND_TYPE_STATUS);
        } else if ("leetcode.find.Lists".equals(id)) {
            tags = ViewManager.getFilter(Constant.FIND_TYPE_LISTS);
        } else if ("leetcode.find.Tags".equals(id)) {
            tags = ViewManager.getFilter(Constant.FIND_TYPE_TAGS);
        } else if ("leetcode.find.Category".equals(id)) {
            tags = ViewManager.getFilter(Constant.FIND_TYPE_CATEGORY);
        }

        return tags;
    }

}
