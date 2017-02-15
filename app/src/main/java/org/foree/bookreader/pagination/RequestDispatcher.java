package org.foree.bookreader.pagination;

import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.data.cache.PaginationCache;
import org.foree.bookreader.data.event.PaginationEvent;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;
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
                String url = request.getUrl();

                Chapter chapter = PaginationCache.getInstance().get(url);

                if (chapter == null) {
                    downloadChapter(request, url);
                } else {
                    EventBus.getDefault().post(new PaginationEvent(chapter, PaginationEvent.STATE_SUCCESS));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadChapter(final ChapterRequest request, final String url) {
        if (url != null && !url.isEmpty()) {
            AbsWebParser absWebParser = WebParserManager.getInstance().getWebParser(url);
            absWebParser.getChapterContents(url, new NetCallback<Chapter>() {
                @Override
                public void onSuccess(Chapter chapter) {
                    if (chapter.getContents() != null) {
                        PaginateCore.splitPage(request.getPaginationArgs(), chapter);

                        // put cache
                        PaginationCache.getInstance().put(url, chapter);

                        // post
                        EventBus.getDefault().post(new PaginationEvent(chapter, PaginationEvent.STATE_SUCCESS));
                    } else {
                        EventBus.getDefault().post(new PaginationEvent(null, PaginationEvent.STATE_FAILED));
                    }
                }

                @Override
                public void onFail(String msg) {
                    EventBus.getDefault().post(new PaginationEvent(null, PaginationEvent.STATE_FAILED));
                }
            });
        }
    }

}
