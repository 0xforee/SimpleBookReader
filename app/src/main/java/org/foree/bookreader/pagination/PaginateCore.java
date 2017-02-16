package org.foree.bookreader.pagination;

import android.text.Layout;
import android.text.StaticLayout;

import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.utils.StringUtils;

/**
 * Created by foree on 17-1-12.
 * 分页的核心代码
 */

public class PaginateCore {

    public static void splitPage(PaginationArgs paginationArgs, Chapter chapter) {
        // 1st. format content first
        chapter.setContents(StringUtils.trim(chapter.getContents()));

        // 2nd. split Page
        chapter.clearPages();
        final StaticLayout layout = new StaticLayout(chapter.getContents(),
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
                chapter.addPage(StringUtils.trim(text.subSequence(startOffset, layout.getLineStart(i)).toString()));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + paginationArgs.getmHeight();
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                chapter.addPage(StringUtils.trim(text.subSequence(startOffset, layout.getLineEnd(i)).toString()));
                return;
            }
        }
    }
}