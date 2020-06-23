package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Tag;
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
        String id = e.getActionManager().getId(this);
        List<Tag> tags = getTags(id);

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                if(tag.isSelect()){
                    e.getPresentation().setIcon(LeetCodeEditorIcons.FILTER);
                    return;
                }
            }
        }

        e.getPresentation().setIcon(null);

    }


    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {

        List<AnAction> anActionList = Lists.newArrayList();

        String id = anActionEvent.getActionManager().getId(this);

        List<Tag> tags = getTags(id);

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                anActionList.add(new FindTagAction(tag.getName(), tag));
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
        }

        return tags;
    }

}
