package com.shuzijun.leetcode.plugin.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FindAndFilterTest {

    @Test
    public void appliesEveryNavigatorFilterAndClearsOnlyFilterState() {
        PageInfo<Object> pageInfo = new PageInfo<>(4, 50);
        pageInfo.setCategorySlug("Algorithms");
        pageInfo.getFilters().setSearchKeywords("two sum");
        pageInfo.disposeFilters("difficulty", "EASY", true);
        pageInfo.disposeFilters("status", "AC", true);
        pageInfo.disposeFilters("listId", "top-interview-150", true);
        pageInfo.disposeFilters("tags", "array", true);
        pageInfo.disposeFilters("tags", "hash-table", true);
        pageInfo.disposeFilters("orderBy", "FRONTEND_ID", true);
        pageInfo.disposeFilters("sortOrder", "ASCENDING", true);

        assertEquals("Algorithms", pageInfo.getCategorySlug());
        assertEquals("two sum", pageInfo.getFilters().getSearchKeywords());
        assertEquals("EASY", pageInfo.getFilters().getDifficulty());
        assertEquals("AC", pageInfo.getFilters().getStatus());
        assertEquals("top-interview-150", pageInfo.getFilters().getListId());
        assertEquals(Arrays.asList("array", "hash-table"), pageInfo.getFilters().getTags());

        pageInfo.clearFilter();

        assertEquals(1, pageInfo.getPageIndex());
        assertEquals("", pageInfo.getCategorySlug());
        assertEquals("two sum", pageInfo.getFilters().getSearchKeywords());
        assertNull(pageInfo.getFilters().getDifficulty());
        assertNull(pageInfo.getFilters().getStatus());
        assertNull(pageInfo.getFilters().getListId());
        assertNull(pageInfo.getFilters().getTags());
        assertEquals("FRONTEND_ID", pageInfo.getFilters().getOrderBy());
        assertEquals("ASCENDING", pageInfo.getFilters().getSortOrder());
    }

    @Test
    public void supportsMultiSelectTagsAndSingleSelectCompanyLists() {
        PageInfo<Object> pageInfo = new PageInfo<>(1, 50);
        pageInfo.disposeFilters("tags", "array", true);
        pageInfo.disposeFilters("tags", "hash-table", true);
        pageInfo.disposeFilters("tags", "array", false);
        pageInfo.disposeFilters("listId", "byte-dance", true);
        pageInfo.disposeFilters("listId", "bytedance", true);

        assertEquals(Arrays.asList("hash-table"), pageInfo.getFilters().getTags());
        assertEquals("bytedance", pageInfo.getFilters().getListId());
        pageInfo.disposeFilters("tags", "hash-table", false);
        assertNull(pageInfo.getFilters().getTags());
    }

    @Test
    public void ignoresUnknownFilterKeysAndReportsWhetherAllFiltersAreEmpty() {
        PageInfo<Object> pageInfo = new PageInfo<>(1, 50);

        assertTrue(pageInfo.isNoFilter());
        pageInfo.disposeFilters("unknown", "value", true);
        assertTrue(pageInfo.isNoFilter());

        pageInfo.disposeFilters("searchKeywords", "two sum", true);
        assertFalse(pageInfo.isNoFilter());
        pageInfo.disposeFilters("searchKeywords", "", false);
        assertTrue(pageInfo.isNoFilter());
    }

    @Test
    public void rotatesSortStateAndResetsOtherSorts() {
        Find find = new Find();
        Sort title = new Sort("Title", "FRONTEND_ID");
        Sort difficulty = new Sort("Difficulty", "DIFFICULTY");
        find.addSort("title", title);
        find.addSort("difficulty", difficulty);

        find.operationType("title");
        assertEquals(1, title.getType());
        assertEquals(0, difficulty.getType());
        find.operationType("title");
        assertEquals(2, title.getType());
        find.operationType("difficulty");
        assertEquals(0, title.getType());
        assertEquals(1, difficulty.getType());
    }

    @Test
    public void retainsSelectionsWhenRefreshingFilterChoices() {
        Tag selected = tag("array", true);
        Tag unselected = tag("graph", false);
        Find find = new Find();
        find.addFilter("tags", Arrays.asList(selected, unselected));

        Tag refreshedSelected = tag("array", false);
        Tag refreshedNew = tag("tree", false);
        find.resetFilterData("tags", Arrays.asList(refreshedSelected, refreshedNew));

        assertTrue(refreshedSelected.isSelect());
        assertFalse(refreshedNew.isSelect());
    }

    private static Tag tag(String slug, boolean selected) {
        Tag tag = new Tag();
        tag.setSlug(slug);
        tag.setSelect(selected);
        return tag;
    }
}
