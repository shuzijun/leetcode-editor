package com.shuzijun.leetcode.plugin.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageInfoTest {

    @Test
    public void calculatesOffsetsAndKeepsPageWithinBoundsWhenPageSizeChanges() {
        PageInfo<Object> pageInfo = new PageInfo<>(2, 50);
        pageInfo.setRowTotal(100);

        assertEquals(50, pageInfo.getSkip());
        assertEquals(2, pageInfo.getPageTotal());

        pageInfo.setPageSize(100);

        assertEquals(1, pageInfo.getPageIndex());
        assertEquals(0, pageInfo.getSkip());
        assertEquals(1, pageInfo.getPageTotal());
    }

    @Test
    public void clearsFiltersAndResetsToFirstPage() {
        PageInfo<Object> pageInfo = new PageInfo<>(3, 50);
        pageInfo.setCategorySlug("Algorithms");
        pageInfo.disposeFilters("difficulty", "EASY", true);
        pageInfo.disposeFilters("orderBy", "TITLE", true);

        pageInfo.clearFilter();

        assertEquals(1, pageInfo.getPageIndex());
        assertEquals("", pageInfo.getCategorySlug());
        assertEquals(null, pageInfo.getFilters().getDifficulty());
        assertEquals("TITLE", pageInfo.getFilters().getOrderBy());
    }
}
