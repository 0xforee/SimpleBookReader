package org.foree.bookreader.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.data.dao.BReaderContract;

/**
 * Created by foree on 17-2-25.
 * 章节列表的ListView的Adapter
 */

public class ContentAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private Cursor cursor;

    public ContentAdapter(Context context, Cursor cursor) {
        mContext = context;
        this.cursor = cursor;
        layoutInflater = LayoutInflater.from(mContext);

    }

    @Override
    public int getCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // cache contentView
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_content_item_holder, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        cursor.moveToPosition(position);

        // set view
        viewHolder.checkBox.setSelected(true);
        viewHolder.textView.setText(cursor.getString(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CHAPTER_TITLE)));

        if (cursor.getInt(cursor.getColumnIndex(BReaderContract.Chapters.COLUMN_NAME_CACHED)) == 1) {
            viewHolder.textView.setTextColor(mContext.getResources().getColor(R.color.colorChapterOfflined));
        } else {
            viewHolder.textView.setTextColor(mContext.getResources().getColor(R.color.colorChapterUnlined));

        }

        return convertView;
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
