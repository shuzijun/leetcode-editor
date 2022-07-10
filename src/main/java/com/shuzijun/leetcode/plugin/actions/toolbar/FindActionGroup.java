package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Find;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import icons.LeetCodeEditorIcons;

import java.util.List;

/**
 * @author shuzijun
 */
public class FindActionGroup extends ActionGroup implements DumbAware {

    private int i = 0;

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    @Override
    public void update(AnActionEvent e) {
        NavigatorAction navigatorAction = WindowFactory.getDataContext(e.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);

        String id = e.getActionManager().getId(this);
        List<Tag> tags = getTags(id, navigatorAction.getFind());

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                if (tag.isSelect()) {
                    e.getPresentation().setIcon(LeetCodeEditorIcons.FILTER);
                    navigatorAction.updateUI();
                    return;
                }
            }
        }
        e.getPresentation().setIcon(null);
        navigatorAction.updateUI();
    }


    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {
        List<AnAction> anActionList = Lists.newArrayList();
        String id = anActionEvent.getActionManager().getId(this);
        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        List<Tag> tags = getTags(id, navigatorAction.getFind());

        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                anActionList.add(new FindTagAction(tag.getName(), tag, tags, onlyOne(id), getFilterKey(id)));
            }
        }
        AnAction[] anActions = new AnAction[anActionList.size()];
        anActionList.toArray(anActions);
        return anActions;
    }

    private List<Tag> getTags(String id, Find find) {
        return find.getFilter(getKey(id));
    }

    private boolean onlyOne(String id) {
        if (PluginConstant.LEETCODE_FIND_TAGS.equals(id)) {
            return false;
        }
        if (PluginConstant.LEETCODE_ALL_FIND_TAGS.equals(id)) {
            return false;
        }

        return true;
    }

    private String getFilterKey(String id) {
        String key = getKey(id);
        if (Constant.FIND_TYPE_LISTS.equalsIgnoreCase(key)) {
            return "listId";
        } else if (Constant.FIND_TYPE_CATEGORY.equalsIgnoreCase(key)) {
            return "categorySlug";
        } else if (Constant.CODETOP_FIND_TYPE_COMPANY.equalsIgnoreCase(key)) {
            return "listId";
        } else {
            return key;
        }
    }

    private String getKey(String id) {
        return id.replace(PluginConstant.LEETCODE_FIND_PREFIX, "").replace(PluginConstant.LEETCODE_ALL_FIND_PREFIX, "").replace(PluginConstant.LEETCODE_CODETOP_FIND_PREFIX, "");
    }
}
