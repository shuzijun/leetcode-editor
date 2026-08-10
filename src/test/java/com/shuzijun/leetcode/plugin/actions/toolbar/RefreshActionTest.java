package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Find;
import com.shuzijun.leetcode.plugin.model.Sort;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RefreshActionTest {

    @Test
    public void loadsDataWhenTheNavigatorFilterModelIsNotInitialized() {
        RecordingNavigatorAction navigatorAction = new RecordingNavigatorAction(null);

        RefreshAction.refresh(navigatorAction, "leetcode.cn");

        assertEquals(1, navigatorAction.loadCalls);
        assertEquals(0, navigatorAction.clearCalls);
    }

    @Test
    public void preservesFiltersAndSortStateWhenRefreshing() {
        Find find = new Find();
        Sort titleSort = new Sort("Title", "FRONTEND_ID");
        find.addSort("title", titleSort);
        find.operationType("title");
        RecordingNavigatorAction navigatorAction = new RecordingNavigatorAction(find);

        RefreshAction.refresh(navigatorAction, "leetcode.cn");

        assertEquals(1, titleSort.getType());
        assertEquals(1, navigatorAction.loadCalls);
        assertEquals(0, navigatorAction.clearCalls);
    }

    @Test
    public void ignoresRefreshBeforeTheNavigatorActionExists() {
        RefreshAction.refresh(null, "leetcode.cn");
    }

    private static class RecordingNavigatorAction extends NavigatorAction.Adapter<Object> {

        private final Find find;
        private int loadCalls;
        private int clearCalls;

        private RecordingNavigatorAction(Find find) {
            this.find = find;
        }

        @Override
        public Find getFind() {
            return find;
        }

        @Override
        public void loadServiceData() {
            loadCalls++;
        }

        @Override
        public void findClear() {
            clearCalls++;
        }
    }
}
