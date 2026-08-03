package com.shuzijun.leetcode.plugin.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class PageInfo<T> {

    private int pageIndex;
    private int pageSize;
    private int rowTotal;

    private String categorySlug = "";

    private Filters filters = new Filters();

    private List<T> rows;

    public PageInfo() {
    }

    public PageInfo(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        if (rowTotal <= 0) {
            pageIndex = 1;
        } else if (pageIndex > getPageTotal()) {
            pageIndex = getPageTotal();
        }
    }

    public int getPageTotal() {
        return (rowTotal / pageSize) + ((rowTotal % pageSize) > 0 ? 1 : 0);
    }

    public int getRowTotal() {
        return rowTotal;
    }

    public void setRowTotal(int rowTotal) {
        this.rowTotal = rowTotal;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public int getSkip() {
        return (pageIndex - 1) * pageSize;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public Filters getFilters() {
        return filters;
    }

    public void disposeFilters(String key, String value, boolean select) {
        filters.update(key, value, select);
    }

    public void clear() {
        this.pageIndex = 1;
        this.categorySlug = "";
        this.filters.clear();
    }

    public void clearFilter() {
        this.pageIndex = 1;
        this.categorySlug = "";
        this.filters.clearFilter();
    }

    public boolean isNoFilter() {
        return StringUtils.isBlank(categorySlug) && filters.isNoFilter();
    }

    public static class Filters {
        private String searchKeywords;
        private String orderBy;
        private String sortOrder;
        private String difficulty;
        private String status;
        private String listId;
        private List<String> tags;

        public String getSearchKeywords() {
            return searchKeywords;
        }

        public void setSearchKeywords(String searchKeywords) {
            this.searchKeywords = searchKeywords;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getListId() {
            return listId;
        }

        public void setListId(String listId) {
            this.listId = listId;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public void clear() {
            this.orderBy = null;
            this.sortOrder = null;
            this.difficulty = null;
            this.status = null;
            this.listId = null;
            this.tags = null;
        }

        public void clearFilter() {
            this.difficulty = null;
            this.status = null;
            this.listId = null;
            this.tags = null;
        }
        @JSONField(serialize = false)
        public boolean isNoFilter() {
            return StringUtils.isBlank(searchKeywords)
                    && StringUtils.isBlank(orderBy)
                    && StringUtils.isBlank(sortOrder)
                    && StringUtils.isBlank(difficulty)
                    && StringUtils.isBlank(status)
                    && StringUtils.isBlank(listId)
                    && (tags == null || tags.isEmpty());
        }

        private void update(String key, String value, boolean select) {
            if ("searchKeywords".equalsIgnoreCase(key)) {
                searchKeywords = select ? value : null;
            } else if ("orderBy".equalsIgnoreCase(key)) {
                orderBy = select ? value : null;
            } else if ("sortOrder".equalsIgnoreCase(key)) {
                sortOrder = select ? value : null;
            } else if ("difficulty".equalsIgnoreCase(key)) {
                difficulty = select ? value : null;
            } else if ("status".equalsIgnoreCase(key)) {
                status = select ? value : null;
            } else if ("listId".equalsIgnoreCase(key)) {
                listId = select ? value : null;
            } else if ("tags".equalsIgnoreCase(key)) {
                updateTags(value, select);
            }
        }

        private void updateTags(String value, boolean select) {
            if (select) {
                if (tags == null) {
                    tags = new ArrayList<>();
                }
                tags.add(value);
            } else if (tags != null) {
                tags.remove(value);
                if (tags.isEmpty()) {
                    tags = null;
                }
            }
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}
