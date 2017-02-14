package org.foree.bookreader.pagination;

import android.text.Layout;
import android.text.StaticLayout;

import org.foree.bookreader.data.book.Article;
import org.foree.bookreader.utils.StringUtils;

/**
 * Created by foree on 17-1-12.
 * 分页的核心代码
 */

public class PaginateCore {

    public static void splitPage(PaginationArgs paginationArgs, Article article) {
        // 1st. format content first
        article.setContents(StringUtils.trim(article.getContents()));

        // 2nd. split Page
        final StaticLayout layout = new StaticLayout(article.getContents(),
                paginationArgs.getmPaint(),
                paginationArgs.getmWidth(),
                Layout.Alignment.ALIGN_NORMAL,
                paginationArgs.getmSpacingMult(),
                paginationArgs.getmSpacingAdd(),
                paginationArgs.ismIncludePad());

        final int lines = layout.getLineCount();
        final CharSequence text = layout.getText();
        int startOffset = 0;
        int height = paginationArgs.getmHeight();

        for (int i = 0; i < lines; i++) {
            if (height < layout.getLineBottom(i)) {
                // When the splitPage height has been exceeded
                article.addPage(StringUtils.trim(text.subSequence(startOffset, layout.getLineStart(i)).toString()));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + paginationArgs.getmHeight();
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                article.addPage(StringUtils.trim(text.subSequence(startOffset, layout.getLineEnd(i)).toString()));
                return;
            }
        }
    }
}