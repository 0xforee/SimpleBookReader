package org.foree.bookreader.pagination;

import android.support.annotation.NonNull;

/**
 * Created by foree on 17-2-7.
 */

public class ChapterRequest implements Comparable {
    private String url;
    private PaginationArgs paginationArgs;
    // 判断是缓存还是当前章节请求
    private boolean current;
    // 请求的优先级，默认为2，数字越小优先级越高
    private int priority;

    public ChapterRequest(String url, PaginationArgs paginationArgs, boolean current) {
        this.url = url;
        this.paginationArgs = paginationArgs;
        this.current = current;

        // 如果是当前章节，优先级最高0.不是当前章节，优先级低,为2
        this.priority = current ? 0 : 2;
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
    public int compareTo(@NonNull Object another) {
        // 返回负数，代表优先级高
        if (another instanceof ChapterRequest) {
            return (this.priority < ((ChapterRequest) another).priority) ? -1 : 1;
        }
        return 0;
    }
}
