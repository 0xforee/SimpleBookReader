package org.foree.bookreader.pagination;

import org.foree.bookreader.book.Article;
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
    private PriorityBlockingQueue<ArticleRequest> mPriorityQueue;

    public RequestDispatcher(PriorityBlockingQueue<ArticleRequest> priorityQueue) {
        this.mPriorityQueue = priorityQueue;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                ArticleRequest request = mPriorityQueue.take();
                String url = request.getUrl();

                Pagination pagination = PaginationCache.getInstance().get(url);

                if (pagination == null) {
                    downloadArticle(request, url);
                } else {
                    EventBus.getDefault().post(new PaginationEvent(pagination, PaginationEvent.STATE_SUCCESS, url));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadArticle(final ArticleRequest request, final String url) {
        if (url != null && !url.isEmpty()) {
            AbsWebParser absWebParser = WebParserManager.getInstance().getWebParser(url);
            absWebParser.getArticle(url, new NetCallback<Article>() {
                @Override
                public void onSuccess(Article data) {
                    Pagination pagination = new Pagination(request.getPaginationArgs());
                    if (data.getContents() != null) {
                        pagination.clear();
                        pagination.splitPage(data.getContents());

                        // put cache
                        PaginationCache.getInstance().put(url, pagination);

                        // post
                        EventBus.getDefault().post(new PaginationEvent(pagination, PaginationEvent.STATE_SUCCESS, url));
                    } else {
                        EventBus.getDefault().post(new PaginationEvent(null, PaginationEvent.STATE_FAILED, url));
                    }
                }

                @Override
                public void onFail(String msg) {
                    EventBus.getDefault().post(new PaginationEvent(null, PaginationEvent.STATE_FAILED, url));
                }
            });
        }
    }

}
