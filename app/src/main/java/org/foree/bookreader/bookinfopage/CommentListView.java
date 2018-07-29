package org.foree.bookreader.bookinfopage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author foree
 * @date 2018/7/29
 * @description
 */
public class CommentListView extends ListView {
    public CommentListView(Context context) {
        super(context);
    }

    public CommentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
