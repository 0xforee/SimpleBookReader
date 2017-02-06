package org.foree.bookreader.pagination;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 17-1-12.
 */

public class Pagination {

    private PaginationArgs paginationArgs;

    private final List<CharSequence> mPages;

    public Pagination(PaginationArgs paginationArgs) {
        this.paginationArgs = paginationArgs;
        this.mPages = new ArrayList<>();
    }

    public void splitPage(CharSequence contents) {

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
                addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + paginationArgs.getmHeight();
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                addPage(text.subSequence(startOffset, layout.getLineEnd(i)));
                return;
            }
        }
    }

    private void addPage(CharSequence text) {
        mPages.add(text);
    }

    public int size() {
        return mPages.size();
    }

    public String get(int index) {
        return (index >= 0 && index < mPages.size()) ? mPages.get(index).toString() : null;
    }

    public void clear() {
        mPages.clear();
    }

    private List<CharSequence> getPages() {
        return mPages;
    }

    public void switchTo(Pagination other) {
        mPages.clear();
        mPages.addAll(other.getPages());
    }
}