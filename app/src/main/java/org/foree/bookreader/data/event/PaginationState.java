package org.foree.bookreader.data.event;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationState {

    int state;

    // loading state
    public static final int STATE_FAILED = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;

    public PaginationState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
