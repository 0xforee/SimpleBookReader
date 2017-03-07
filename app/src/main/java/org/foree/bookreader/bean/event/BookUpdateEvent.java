package org.foree.bookreader.bean.event;

/**
 * Created by foree on 17-2-24.
 */

public class BookUpdateEvent {
    private int updatedNum;

    public BookUpdateEvent(int updatedNum) {
        this.updatedNum = updatedNum;
    }

    public int getUpdatedNum() {
        return updatedNum;
    }
}
