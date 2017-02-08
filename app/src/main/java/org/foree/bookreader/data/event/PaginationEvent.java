package org.foree.bookreader.data.event;

import org.foree.bookreader.pagination.Pagination;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationEvent {

    private Pagination pagination;
    private int state;
    private String url;

    // loading state
    public static final int STATE_FAILED = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;

    public PaginationEvent(Pagination pagination, int state, String url) {
        this.pagination = pagination;
        this.state = state;
        this.url = url;
    }

    public int getState() {
        return state;
    }

    public String getUrl() {
        return url;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
