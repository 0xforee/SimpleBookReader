package org.foree.bookreader.pagination;

import org.foree.bookreader.book.Article;
import org.foree.bookreader.data.event.PaginationState;
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
                final ArticleRequest request = mPriorityQueue.take();
                String url = request.getUrl();
                final Pagination pagination = request.getPagination();

                if (url != null && !url.isEmpty()) {
                    AbsWebParser absWebParser = WebParserManager.getInstance().getWebParser(url);
                    absWebParser.getArticle(url, new NetCallback<Article>() {
                        @Override
                        public void onSuccess(Article data) {
                            pagination.clear();
                            pagination.splitPage(data.getContents());
                            request.setPagination(pagination);
                            EventBus.getDefault().post(new PaginationState(PaginationState.STATE_SUCCESS));
                        }

                        @Override
                        public void onFail(String msg) {

                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
