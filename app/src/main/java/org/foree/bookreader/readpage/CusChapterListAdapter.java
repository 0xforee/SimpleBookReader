package org.foree.bookreader.readpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.book.Chapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CusChapterListAdapter extends AbstractItemListAdapter {

    public CusChapterListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.lv_content_item_holder, parent, false);
        }

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_chapter_title);
        Chapter chapter = (Chapter) getData().get(position);
        // set view
        tvTitle.setText(chapter.getChapterTitle());

        if (chapter.isOffline()) {
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorChapterOfflined));
        } else {
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorChapterUnlined));

        }

        // set current position color
        if (getSelectedPosition() == position) {
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
        return convertView;
    }
}
