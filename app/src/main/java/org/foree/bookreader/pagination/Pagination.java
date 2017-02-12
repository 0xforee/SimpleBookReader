package org.foree.bookreader.pagination;

import android.text.Layout;
import android.text.StaticLayout;

import org.foree.bookreader.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by foree on 17-1-12.
 */

public class Pagination {

    private PaginationArgs paginationArgs;

    private final ArrayList<String> mPages;

    public Pagination(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;
        this.mPages = new ArrayList<>();
    }

    public void splitPage(CharSequence contents) {
        contents = StringUtils.trim(contents.toString());
        final StaticLayout layout = new StaticLayout(contents,
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
                addPage(StringUtils.trim(text.subSequence(startOffset, layout.getLineStart(i)).toString()));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + paginationArgs.getmHeight();
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                addPage(StringUtils.trim(text.subSequence(startOffset, layout.getLineEnd(i)).toString()));
                return;
            }
        }
    }

    private void addPage(String text) {
        mPages.add(text);
    }

    public int size() {
        return mPages.size();
    }

    public String get(int index) {
        return (index >= 0 && index < mPages.size()) ? mPages.get(index) : null;
    }

    public void clear() {
        mPages.clear();
    }

    public ArrayList<String> getPages() {
        return mPages;
    }

    public void switchTo(Pagination other) {
        mPages.clear();
        mPages.addAll(other.getPages());
    }
}