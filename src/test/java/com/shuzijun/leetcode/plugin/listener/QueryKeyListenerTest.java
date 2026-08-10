package com.shuzijun.leetcode.plugin.listener;

import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueryKeyListenerTest {

    @Test
    public void appliesTrimmedSearchAndReturnsToFirstPage() {
        RecordingNavigatorAction action = new RecordingNavigatorAction();
        action.pageInfo.setPageIndex(4);

        QueryKeyListener.applySearch(action, "  two sum  ");

        assertEquals("two sum", action.pageInfo.getFilters().getSearchKeywords());
        assertEquals(1, action.pageInfo.getPageIndex());
    }

    @Test
    public void clearsSearchForBlankInput() {
        RecordingNavigatorAction action = new RecordingNavigatorAction();
        action.pageInfo.getFilters().setSearchKeywords("old");

        QueryKeyListener.applySearch(action, "   ");

        assertNull(action.pageInfo.getFilters().getSearchKeywords());
        assertEquals(1, action.pageInfo.getPageIndex());
    }

    private static final class RecordingNavigatorAction extends NavigatorAction.Adapter<Object> {
        private final PageInfo<Object> pageInfo = new PageInfo<>(1, 20);

        @Override
        public PageInfo<Object> getPageInfo() {
            return pageInfo;
        }
    }
}
