package org.foree.bookreader.pagination;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by foree on 17-2-7.
 */

public class RequestQueue {

    private PriorityBlockingQueue<ChapterRequest> blockingQueue = new PriorityBlockingQueue<>();

    private int maxThreadCount = Runtime.getRuntime().availableProcessors() + 1;

    private RequestDispatcher mDispatchers[];

    private void dispatcher() {
        mDispatchers = new RequestDispatcher[maxThreadCount];
        for (int i = 0; i < maxThreadCount; i++) {
            mDispatchers[i] = new RequestDispatcher(blockingQueue);
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

    private void stop() {
        if (mDispatchers != null) {
            for (RequestDispatcher dispatcher : mDispatchers) {
                if (!dispatcher.isInterrupted()) {
                    dispatcher.interrupt();
                }
            }
        }

    }

}
