package org.foree.bookreader.readpage;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.dao.BReaderContract;

/**
 * Created by foree on 17-2-25.
 * 章节列表的ListView的Adapter
 */

public class ContentAdapter extends CursorAdapter {
    private int selectedPosition;

    public ContentAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        selectedPosition = 0;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View rootView = layoutInflater.inflate(R.layout.listview_content_item_holder, null);
        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);

        return rootView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // set view
        viewHolder.textView.setText(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE)));

        if (cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1) {
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.colorChapterOfflined));
        } else {
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.colorChapterUnlined));

        }

        // set current position color
        if (selectedPosition == cursor.getPosition()) {
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
    }

    private static class ViewHolder {
        private TextView textView;

        ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.tv_chapter_title);
        }
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }
}
