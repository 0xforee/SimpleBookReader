package org.foree.bookreader.parser;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Rank;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author foree
 * @date 2018/8/19
 * @description
 */
public class ThirdSourceParser extends AbstractWebParser {
    private static final String TAG = "ThirdSourceParser";
    private ThirdWebInfo mWebInfo;

    public ThirdSourceParser(ThirdWebInfo webInfo) {
        mWebInfo = webInfo;
    }

    @Override
    AbstractWebInfo getWebInfo() {
        return null;
    }

    /**
     * search book
     *
     * @param keyword book name or author
     * @param params  http request params:
     *                query: keyword
     *                start: start offset
     *                limit: results limit
     * @return book list
     */
    @Override
    public List<Book> searchBook(String keyword, Map<String, String> params) {

        return parseSearchBook(keyword);
    }

    /**
     * get book detail
     *
     * @param bookUrl book id or url
     * @return book object
     */
    @Override
    public Book getBookInfo(String bookUrl) {
        return parseBookInfo(bookUrl);
    }

    /**
     * get contents of a book
     *
     * @param bookUrl     book id or url
     * @param contentsUrl contents id (same as sourceId)
     * @return chapter list
     */
    @Override
    public List<Chapter> getContents(String bookUrl, String contentsUrl) {
        return parseBookInfo(bookUrl).getChapters();
    }

    /**
     * get chapter detail
     *
     * @param bookUrl    book id or url
     * @param chapterUrl chapter id or url
     * @return chapter object
     */
    @Override
    public Chapter getChapter(String bookUrl, String chapterUrl) {
        Chapter chapter = new Chapter();
        chapter.setBookUrl(wrapSplitKey(bookUrl));
        chapter.setChapterUrl(wrapSplitKey(chapterUrl));
        chapter.setContents(parseChapter(chapterUrl));
        return chapter;
    }

    /**
     * get all rank info
     *
     * @return rank list
     */
    @Override
    public List<Rank> getHomePageInfo() {
        return null;
    }

    private List<Book> parseSearchBook(String keyword) throws SearchUrlInvalidException {
        Elements books = null;
        List<Book> bookList = new ArrayList<>();
        try {
            // search url
            String searchUrl = mWebInfo.getRuleSearchUrl();

            // invalid search url
            if (!searchUrl.startsWith("http")) {
                throw new SearchUrlInvalidException();
            }

            searchUrl = searchUrl.replace("searchKey", keyword);

            Document doc;

            if (searchUrl.contains("@") || searchUrl.contains("searchPage")) {
                throw new SearchUrlInvalidException();
                // post and get location
//                doc = Jsoup.connect(searchUrl.split("@")[0]).requestBody(searchUrl.split("@")[1]).followRedirects(true).post();
            } else {
                // load search result
                doc = Jsoup.connect(searchUrl).ignoreContentType(true).get();
            }

            Element body = doc.body();

            // search list
            books = getElements(mWebInfo.getRuleSearchList(), body);

            for (Element target : books) {
                try {
                    // book name
                    String bookName = getElementString(mWebInfo.getRuleSearchName(), target);
                    Log.d(TAG, "bookName = " + bookName);
                    if ("".equals(bookName)) {
                        continue;
                    }

                    // book author
                    String bookAuthor = getElementString(mWebInfo.getRuleSearchAuthor(), target);
                    Log.d(TAG, "bookAuthor = " + bookAuthor);

                    // last chapter
                    String lastChapter = getElementString(mWebInfo.getRuleSearchLastChapter(), target);
                    Log.d(TAG, "lastChapter = " + lastChapter);

                    // search kind
                    String searchKind = getElementString(mWebInfo.getRuleSearchKind(), target);
                    Log.d(TAG, "searchKind = " + searchKind);

                    // ruleSearchNoteUrl
                    String ruleSearchNoteUrl = getElementString(mWebInfo.getRuleSearchNoteUrl(), target);
                    Log.d(TAG, "ruleSearchNoteUrl = " + ruleSearchNoteUrl);

                    // ruleSearchCoverUrl
                    String ruleSearchCoverUrl = getElementString(mWebInfo.getRuleSearchCoverUrl(), target);
                    Log.d(TAG, "ruleSearchCoverUrl = " + ruleSearchCoverUrl);

                    Book book = new Book();
                    book.setBookName(bookName);
                    book.setAuthor(bookAuthor);
                    book.setCategory(searchKind);
                    book.setBookUrl(wrapSplitKey(ruleSearchNoteUrl));
                    book.setBookCoverUrl(ruleSearchCoverUrl);

                    bookList.add(book);

                } catch (NoSuchElementException e) {
                    continue;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return bookList;
    }

    private Book parseBookInfo(String bookUrl) {
        Elements chapterElements = null;
        Book book = new Book();
        try {
            Document doc = Jsoup.connect(bookUrl).ignoreContentType(true).get();
            if (doc != null) {

                String bookAuthor = getElementString(mWebInfo.getRuleBookAuthor(), doc.body());
                String bookName = getElementString(mWebInfo.getRuleBookName(), doc.body());
                String description = getElementString(mWebInfo.getRuleIntroduce(), doc.body());
                String coverUrl = getElementString(mWebInfo.getRuleCoverUrl(), doc.body());
                String contentUrl = getElementString(mWebInfo.getRuleChapterUrl(), doc.body());
                if (contentUrl.isEmpty()) {
                    contentUrl = bookUrl;
                }

                book.setAuthor(bookAuthor);
                book.setBookName(bookName);
                book.setDescription(description);
                book.setBookCoverUrl(coverUrl);
                book.setContentUrl(wrapSplitKey(contentUrl));

                // test contents
                List<Chapter> chapters = new ArrayList<>();
                chapterElements = getElements(mWebInfo.getRuleChapterList(), doc.body());
                for (Element target : chapterElements) {
                    // chapter name
                    String chapterName = getElementString(mWebInfo.getRuleChapterName(), target);
                    // chapter url
                    String chapterUrl = getElementString(mWebInfo.getRuleContentUrl(), target);

                    Chapter chapter = new Chapter();
                    chapter.setChapterTitle(chapterName);
                    chapter.setChapterUrl(wrapSplitKey(chapterUrl));
                    chapter.setBookUrl(wrapSplitKey(bookUrl));

                    chapters.add(chapter);
                    if (DEBUG) {
                        Log.d(TAG, "chapterName = " + chapterName + ", chapterUrl = " + chapterUrl);
                    }
                }

                book.setChapters(chapters);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return book;
    }

    private String parseChapter(String chapterUrl) {
        String content = null;
        try {
            Document doc = Jsoup.connect(chapterUrl).ignoreContentType(true).get();
            if (doc != null) {
                content = getElementString(mWebInfo.getRuleBookContent(), doc.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    private Elements getElements(String ruleString, Element target) {
        Elements result = new Elements();
        String[] rulesAll = ruleString.split("!");

        // 匹配规则
        String[] findRules = rulesAll[0].split("@");

        result = getRecursion(findRules, new Elements(target), 0);

        // 忽略规则
        if (rulesAll.length > 1) {
            String[] ignoreRule = rulesAll[1].split(":");

            for (int k = ignoreRule.length - 1; k >= 0; k--) {
                if (ignoreRule[k].equals("%")) {
                    ignoreRule[k] = (result.size() - 1) + "";
                }

                int index = Integer.valueOf(ignoreRule[k]);
                if (result.size() > index) {
                    result.remove(index);
                }
            }
        }

        return result;
    }

    private String getElementString(String ruleString, Element target) {
        if (DEBUG) {
            Log.d(TAG, "getElementString() called with: ruleString = [" + ruleString + "]");
        }
        StringBuilder result = new StringBuilder();
        // regex string
        String[] regexRules = ruleString.split("#");

        String[] rules = regexRules[0].split("@");

        // get all elements
        Elements all = getRecursion(rules, new Elements(target), 0);

        // get text from elements
        String[] subRules = rules[rules.length - 1].split("\\.");

        // last one, get content
        if (all != null && !all.isEmpty()) {
            for (Element el : all) {
                String temp = "";
                switch (subRules[0]) {
                    case "text":
                        temp = el.text() + "\n";
                        break;
                    case "href":
                        temp = el.attr("abs:href");
                        break;
                    case "src":
                        temp = el.attr("abs:src");
                        break;
                    case "textNodes":
                        StringBuilder sb = new StringBuilder();
                        for (TextNode tn : el.textNodes()) {
                            sb.append(tn.text()).append("\n");
                            ;
                        }
                        temp = sb.toString();
                        break;
                    default:
                }

                // remove all space
                temp = temp.replaceAll(" ", "");

                if (regexRules.length > 1) {
                    // use regex replace
                    temp = temp.replaceAll(regexRules[1], "");
                    if (DEBUG) {
                        Log.d(TAG, "after regex: result = " + result);
                    }
                }

                result.append(temp);
            }

        }

        return result.toString();
    }

    private Elements getRecursion(String[] ruleString, Elements target, int level) {
        if (level == ruleString.length) {
            // 如果规则解析到达末尾，结束递归
            return target;
        } else {
            // 计算值
            Elements result = new Elements();
            String[] subRules = ruleString[level].split("\\.");
            if (DEBUG) {
                Log.d(TAG, "subRules = " + Arrays.toString(subRules));
            }
            for (Element el : target) {
                Elements classes = null;
                switch (subRules[0]) {
                    case "id":
                        result.add(el.getElementById(subRules[1]));
                        continue;
                    case "class":
                        classes = (el.getElementsByClass(subRules[1]));
                        break;
                    case "tag":
                        classes = el.getElementsByTag(subRules[1]);
                        break;
                    default:
                        // 如果最后一个字段为text或者href，textNode等，那么结束递归
                        return target;
                }

                if (classes == null || classes.isEmpty()) {
                    continue;
                }
                if (subRules.length > 2) {
                    result.add(classes.get(Integer.valueOf(subRules[2])));
                } else {
                    result.addAll(classes);
                }
            }

            return getRecursion(ruleString, result, level + 1);
        }
    }

    class SearchUrlInvalidException extends RuntimeException {

    }

    @Override
    String wrapSplitKey(String url) {
        return mWebInfo.getBookSourceUrl() + SPLIT_KEY + url;
    }
}
