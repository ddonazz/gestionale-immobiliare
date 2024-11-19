package it.andrea.start.utils;

import java.io.Serializable;
import java.util.Collection;

public class PagedResult<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = -8138842272681687945L;

    private Collection<T> items;
    private Integer itemStart;
    private Integer itemNum;
    private Integer itemCount;
    private Integer pageNum;
    private Integer pageCount;

    public PagedResult() {
    }

    public PagedResult(Collection<T> items, Integer itemStart, Integer itemNum, Integer itemCount, Integer pageNum, Integer pageCount) {
        this.items = items;
        this.itemStart = itemStart;
        this.itemNum = itemNum;
        this.itemCount = itemCount;
        this.pageNum = pageNum;
        this.pageCount = pageCount;
    }

    public Collection<T> getItems() {
        return this.items;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }

    public Integer getItemStart() {
        return this.itemStart;
    }

    public void setItemStart(Integer itemStart) {
        this.itemStart = itemStart;
    }

    public Integer getItemNum() {
        return this.itemNum;
    }

    public void setItemNum(Integer itemNum) {
        this.itemNum = itemNum;
    }

    public Integer getItemCount() {
        return this.itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Integer getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageCount() {
        return this.pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String toString() {
        return "PagedResult [items=" + this.items + ", itemStart=" + this.itemStart + ", itemNum=" + this.itemNum + ", itemCount=" + this.itemCount + ", pageNum=" + this.pageNum + ", pageCount=" + this.pageCount + "]";
    }

}
