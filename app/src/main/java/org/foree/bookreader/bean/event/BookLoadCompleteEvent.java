package org.foree.bookreader.bean.event;

public class BookLoadCompleteEvent {
    private boolean mState;
    public BookLoadCompleteEvent(boolean successful) {
        mState = successful;
    }

    public boolean getState(){
        return mState;
    }
}
