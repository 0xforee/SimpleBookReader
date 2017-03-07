package org.foree.bookreader.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by foree on 17-2-9.
 */

public class RoundCornerImageView extends ImageView {
    Path clipPath;

    public RoundCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        clipPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = this.getWidth();
        int h = this.getHeight();
        // draw a round
        clipPath.addRoundRect(new RectF(0, 0, w, h), 5f, 5f, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}
