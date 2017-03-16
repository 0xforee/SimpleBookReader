package org.foree.bookreader.pagination;

/**
 * Created by foree on 17-2-7.
 */

public class ChapterRequest implements Comparable {
    private String url;
    private PaginationArgs paginationArgs;
    // 判断是缓存还是当前章节请求
    private boolean current;

    public ChapterRequest(String url, PaginationArgs paginationArgs, boolean current) {
        this.url = url;
        this.paginationArgs = paginationArgs;
        this.current = current;
    }

    public String getUrl() {
        return url;
    }

    public PaginationArgs getPaginationArgs() {
        return paginationArgs;
    }

    public boolean isCurrent() {
        return current;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
