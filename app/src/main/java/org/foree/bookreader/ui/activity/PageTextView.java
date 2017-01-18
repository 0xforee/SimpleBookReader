package org.foree.bookreader.ui.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by foree on 17-1-12.
 */

public class PageTextView extends TextView {
    private static final String TEXT = "ap卡了ξτβбпшㄎㄊěǔぬも┰┠№＠↓ap卡了ξτβбпшㄎㄊěǔぬもap卡了ξτβбпшㄎㄊěǔぬもap卡了ξτβбпшㄎㄊěǔぬもap卡了ξτβбпшㄎㄊěǔぬも";
    TextPaint mTextPaint;

    public PageTextView(Context context) {
        super(context);
        init();
    }

    public PageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mTextPaint == null) {
            mTextPaint = new TextPaint();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();

        mTextPaint.setTextSize(50);
        mTextPaint.clearShadowLayer();
        StaticLayout mStaticLayout = new StaticLayout(TEXT, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        mStaticLayout.draw(canvas);
        //canvas.drawText(TEXT, 0, Math.abs(fontMetrics.top), mTextPaint);


    }
}
