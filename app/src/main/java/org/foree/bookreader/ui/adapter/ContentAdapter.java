package org.foree.bookreader.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.data.dao.BReaderContract;

/**
 * Created by foree on 17-2-25.
 * 章节列表的ListView的Adapter
 */

public class ContentAdapter extends CursorAdapter {
    public ContentAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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
        viewHolder.checkBox.setSelected(true);
        viewHolder.textView.setText(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE)));

        if (cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1) {
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.colorChapterOfflined));
        } else {
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.colorChapterUnlined));

        }
    }

    private static class ViewHolder {
        private CheckBox checkBox;
        private TextView textView;

        ViewHolder(View view) {
            checkBox = (CheckBox) view.findViewById(R.id.cb_position);
            textView = (TextView) view.findViewById(R.id.tv_chapter_title);
        }
    }


}
