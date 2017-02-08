package org.foree.bookreader.data.event;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationState {

    int state;
    String url;

    // loading state
    public static final int STATE_FAILED = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;

    public PaginationState(int state, String url) {
        this.state = state;
        this.url = url;
    }

    public int getState() {
        return state;
    }

    public String getUrl() {
        return url;
    }
}
