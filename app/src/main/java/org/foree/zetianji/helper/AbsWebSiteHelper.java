package org.foree.zetianji.helper;

import org.foree.zetianji.NetCallback;
import org.foree.zetianji.book.Novel;

/**
 * Created by foree on 16-7-26.
 */
public abstract class AbsWebSiteHelper {
    public abstract String getWebsiteCharSet();
    public abstract void getNovel(NetCallback<Novel> netCallback);
    public abstract void getChapterContent(String chapter_url, String webChar, NetCallback<String> netCallback);
}
