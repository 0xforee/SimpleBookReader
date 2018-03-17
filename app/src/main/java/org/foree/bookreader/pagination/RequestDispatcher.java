package org.foree.bookreader.pagination;

import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.cache.ChapterCache;
import org.foree.bookreader.bean.event.PaginationEvent;
import org.foree.bookreader.parser.WebParserProxy;
import org.greenrobot.eventbus.EventBus;

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

                Chapter chapter;

                if (request.getUrl() != null && !request.getUrl().isEmpty()) {
                    final ChapterCache chapterCache = PaginationLoader.getInstance().getChapterCache();
                    // get from cache
                    chapter = chapterCache.get(request.getUrl());
                    if (chapter == null) {
                        // download from net
                        chapter = WebParserProxy.getInstance().getChapter("", request.getUrl());
                    }

                    if (chapter != null && chapter.getContents() != null) {
                        // put chapter cache
                        chapterCache.put(request.getUrl(), chapter);

                        PaginateCore.splitPage(request.getPaginationArgs(), chapter);

                    }

                    // post
                    EventBus.getDefault().post(new PaginationEvent(chapter, request.isCurrent()));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
