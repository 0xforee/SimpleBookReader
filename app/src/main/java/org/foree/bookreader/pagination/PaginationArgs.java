package org.foree.bookreader.pagination;

import android.text.TextPaint;

/**
 * Created by foree on 17-2-6.
 * Pagination的参数
 */

public class PaginationArgs {
    private int mWidth;
    private int mHeight;
    private float mSpacingMult;
    private float mSpacingAdd;
    private TextPaint mPaint;
    private boolean mIncludePad;

    public PaginationArgs(int mWidth, int mHeight, float mSpacingMult, float mSpacingAdd, TextPaint mPaint, boolean mIncludePad) {
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mSpacingMult = mSpacingMult;
        this.mSpacingAdd = mSpacingAdd;
        this.mPaint = mPaint;
        this.mIncludePad = mIncludePad;
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public float getmSpacingMult() {
        return mSpacingMult;
    }

    public float getmSpacingAdd() {
        return mSpacingAdd;
    }

    public TextPaint getmPaint() {
        return mPaint;
    }

    public boolean ismIncludePad() {
        return mIncludePad;
    }
}
