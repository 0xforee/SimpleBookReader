package org.foree.bookreader.pagination;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by foree on 17-2-7.
 */

public class RequestQueue {

    private PriorityBlockingQueue<ChapterRequest> blockingQueue = new PriorityBlockingQueue<>();

    private int maxThreadCount = 5;

    private RequestDispatcher mDispatchers[];

    public RequestQueue() {
        mDispatchers = new RequestDispatcher[maxThreadCount];
        for (int i = 0; i < maxThreadCount; i++) {
            RequestDispatcher dispatcher = new RequestDispatcher(blockingQueue);
            mDispatchers[i] = dispatcher;
        }
    }

    public void dispatcher() {
        for (int i = 0; i < maxThreadCount; i++) {
            mDispatchers[i].start();
        }
    }

    public void add(ChapterRequest chapterRequest) {
        if (!blockingQueue.contains(chapterRequest)) {
            blockingQueue.add(chapterRequest);
        }
    }

    public void start() {
        stop();
        dispatcher();
    }

    public void stop() {
        for (int i = 0; i < maxThreadCount; i++) {
            if (!mDispatchers[i].isInterrupted()) {
                mDispatchers[i].interrupt();
            }
        }
    }

}
