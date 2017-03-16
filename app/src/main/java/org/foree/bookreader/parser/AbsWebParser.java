package org.foree.bookreader.parser;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.cache.ChapterCache;
import org.foree.bookreader.bean.event.PaginationEvent;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.pagination.ChapterRequest;
import org.foree.bookreader.pagination.PaginateCore;
import org.foree.bookreader.pagination.PaginationLoader;
import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by foree on 17-1-7.
 * 解析器的公共api接口，用于构建标准的接口
 */

public abstract class AbsWebParser implements IWebParser {
    private static final String TAG = AbsWebParser.class.getSimpleName();

    public interface WebInfo {
        /**
         * 获取目标网站名称
         *
         * @return 网站名称
         */
        String getHostName();

        /**
         * 获取解析网站的网页编码
         *
         * @return 网页编码
         */
        String getWebChar();

        /**
         * 获取目标网站地址
         *
         * @return 网页主机host地址
         */
        String getHostUrl();

        /**
         * 获取搜索api用于传入搜索关键字
         *
         * @return 搜索api
         */
        String getSearchApi();
    }

    abstract WebInfo getWebInfo();

    public void searchBook(final String keywords, final NetCallback<List<Book>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(getWebInfo().getSearchApi() + keywords).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseBookList(doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    public void getBookInfo(final String bookUrl, final NetCallback<Book> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(bookUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseBookInfo(bookUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();

    }

    public Book getBookInfo(final String bookUrl) {
        Document doc;
        Book book = null;
        try {
            doc = Jsoup.connect(bookUrl).get();
            if (doc != null) {
                book = parseBookInfo(bookUrl, doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    public void getChapterList(final String bookUrl, final String contentUrl, final NetCallback<List<Chapter>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(contentUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseChapterList(bookUrl, contentUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    public List<Chapter> getChapterList(final String bookUrl, final String contentUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(contentUrl).get();
            if (doc != null) {
                return parseChapterList(bookUrl, contentUrl, doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getChapterContents(final String chapterUrl, final NetCallback<Chapter> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(chapterUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseChapterContents(chapterUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    int getChapterId(String url) {
        // convert http://m.bxwx9.org/0_168/2512063.html ==> 2512063

        String[] subString = url.split("/|\\.");
        return Integer.parseInt(subString[subString.length - 2]);
    }

    public void getHomePageInfo(final String hostUrl, final NetCallback<List<List<Book>>> netCallback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc;
                try {
                    doc = Jsoup.connect(hostUrl).get();
                    if (netCallback != null && doc != null) {
                        netCallback.onSuccess(parseHostUrl(hostUrl, doc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (netCallback != null) {
                        netCallback.onFail(e.toString());
                    }
                }
            }
        }.start();
    }

    public void downloadChapter(final ChapterRequest request) {
        if (request.getUrl() != null && !request.getUrl().isEmpty()) {
            final ChapterCache chapterCache = PaginationLoader.getInstance().getChapterCache();
            Chapter chapter = chapterCache.get(request.getUrl());
            if (chapter == null) {
                getChapterContents(request.getUrl(), new NetCallback<Chapter>() {
                    @Override
                    public void onSuccess(Chapter chapter) {
                        if (chapter.getContents() != null) {
                            PaginateCore.splitPage(request.getPaginationArgs(), chapter);

                            // put pagination cache
                            //PaginationCache.getInstance().put(url, chapter);

                            // put chapter cache
                            chapterCache.put(request.getUrl(), chapter);

                            // post
                            EventBus.getDefault().post(new PaginationEvent(chapter));
                        } else {
                            EventBus.getDefault().post(new PaginationEvent(null));
                        }
                    }

                    @Override
                    public void onFail(String msg) {
                        EventBus.getDefault().post(new PaginationEvent(null));
                    }
                });
            } else {
                PaginateCore.splitPage(request.getPaginationArgs(), chapter);
                // post
                EventBus.getDefault().post(new PaginationEvent(chapter));
            }
        }
    }
}
