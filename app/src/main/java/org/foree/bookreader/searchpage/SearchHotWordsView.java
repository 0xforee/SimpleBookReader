package org.foree.bookreader.searchpage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.foree.bookreader.R;

/**
 * @author foree
 * @date 2018/9/24
 * @description
 */
public class SearchHotWordsView extends ViewGroup {
    int mExtraWidth, mExtraHeight;

    public SearchHotWordsView(Context context) {
        this(context, null);
    }

    public SearchHotWordsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mExtraHeight = getResources().getDimensionPixelSize(R.dimen.search_hotwords_extra_line_space);
        mExtraWidth = getResources().getDimensionPixelSize(R.dimen.search_hotwords_extra_space);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int containerWidth = getMeasuredWidth();

        int curLine = 1;
        int curWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int heightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);
            int widthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
            child.measure(widthSpec, heightSpec);

            int childWidth = child.getMeasuredWidth();
            if ((curWidth + childWidth) > containerWidth) {
                // layout in new line
                curLine++;
                curWidth = 0;
            }
            curWidth += childWidth + mExtraWidth;
        }

        if(getChildCount() > 0) {
            int childHeight = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(containerWidth, curLine * (childHeight + mExtraHeight));
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int curWidth = 0;
        int curLine = 0;
        int curHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            int childWidth = child.getMeasuredWidth();

            if ((curWidth + childWidth) > width) {
                // layout in new line
                curLine++;
                curWidth = 0;
                curHeight = curLine * (childHeight + mExtraHeight);
            }
            child.layout(curWidth, curHeight, curWidth + childWidth, curHeight + childHeight);
            curWidth += childWidth + mExtraWidth;

        }
    }
}
