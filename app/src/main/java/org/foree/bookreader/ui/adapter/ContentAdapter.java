package org.foree.bookreader.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.data.book.Chapter;

import java.util.List;

/**
 * Created by foree on 17-2-25.
 * 章节列表的ListView的Adapter
 */

public class ContentAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<Chapter> chapters;

    public ContentAdapter(Context context, List<Chapter> chapters) {
        mContext = context;
        this.chapters = chapters;
        layoutInflater = LayoutInflater.from(mContext);

    }

    @Override
    public int getCount() {
        return chapters != null ? chapters.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return chapters.get(position);
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

        // set view
        viewHolder.checkBox.setSelected(true);
        viewHolder.textView.setText(chapters.get(position).getChapterTitle());

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
