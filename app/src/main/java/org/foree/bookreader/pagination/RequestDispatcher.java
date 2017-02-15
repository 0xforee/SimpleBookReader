package org.foree.bookreader.pagination;

import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by foree on 17-2-7.
 */

public class RequestDispatcher extends Thread {
    private PriorityBlockingQueue<ChapterRequest> mPriorityQueue;

    public RequestDispatcher(PriorityBlockingQueue<ChapterRequest> priorityQueue) {
        this.mPriorityQueue = priorityQueue;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                ChapterRequest request = mPriorityQueue.take();

                AbsWebParser absWebParser = WebParserManager.getInstance().getWebParser(request.getUrl());

                absWebParser.downloadChapter(request, request.getUrl());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
