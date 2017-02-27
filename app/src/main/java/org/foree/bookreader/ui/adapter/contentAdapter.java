package org.foree.bookreader.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.foree.bookreader.R;

/**
 * Created by foree on 17-2-25.
 * 章节列表的ListView的Adapter
 */

public class contentAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;

    public contentAdapter(Context context) {
        mContext = context;
        layoutInflater = LayoutInflater.from(mContext);

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
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
        viewHolder.textView.setText("哈哈哈哈");

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
