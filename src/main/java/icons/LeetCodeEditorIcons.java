package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author shuzijun
 */
public interface LeetCodeEditorIcons {

    Icon LEETCODE_TOOL_WINDOW = getIcon("/icons/LeetCode.svg");
    Icon EMPEROR_NEW_CLOTHES = getIcon("/icons/emperor_new_clothes.svg");

    Icon CLEAN = getIcon("/icons/clean.svg");
    Icon CLEAR = getIcon("/icons/clear.svg");
    Icon COLLAPSE = getIcon("/icons/collapse.svg");
    Icon CONFIG = getIcon("/icons/config_lc.svg");
    Icon DESC = getIcon("/icons/desc.svg");
    Icon EDIT_DOC = getIcon("/icons/edit_doc.svg");
    Icon FAVORITE = getIcon("/icons/favorite.svg");
    Icon FILTER = getIcon("/icons/filter.svg");
    Icon FIND = getIcon("/icons/find.svg");
    Icon HELP = getIcon("/icons/help.svg");
    Icon HISTORY = getIcon("/icons/history.svg");
    Icon LOGIN = getIcon("/icons/login.svg");
    Icon LOGOUT = getIcon("/icons/logout.svg");
    Icon POPUP = getIcon("/icons/popup.svg");
    Icon POSITION = getIcon("/icons/position.svg");
    Icon PROGRESS = getIcon("/icons/progress.svg");
    Icon QUESTION = getIcon("/icons/question.svg");
    Icon RANDOM = getIcon("/icons/random.svg");
    Icon REFRESH = getIcon("/icons/refresh.svg");
    Icon RUN = getIcon("/icons/run.svg");
    Icon SOLUTION = getIcon("/icons/solution.svg");
    Icon SUBMIT = getIcon("/icons/submit.svg");
    Icon TIME = getIcon("/icons/time.svg");
    Icon SORT_ASC = getIcon("/icons/sortAsc.svg");
    Icon SORT_DESC = getIcon("/icons/sortDesc.svg");
    Icon NOTE = getIcon("/icons/note.svg");
    Icon LCV = getIcon("/icons/lcv.svg");
    Icon DONATE = getIcon("/icons/donate.svg");
    Icon SHARE = getIcon("/icons/share.svg");
    Icon TOGGLE = getIcon("/icons/toggle.svg");
    Icon SHOW = getIcon("/icons/show.svg");
    Icon PULL = getIcon("/icons/pull.svg");
    Icon PUSH = getIcon("/icons/push.svg");

    static Icon getIcon(String path) {
        return IconLoader.getIcon(path, LeetCodeEditorIcons.class);
    }
}
