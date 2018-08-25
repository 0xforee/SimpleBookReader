package org.foree.bookreader.readpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author foree
 * @date 2018/7/16
 * @description
 */
public class CusSourceListAdapter extends AbstractItemListAdapter {

    public CusSourceListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.lv_source_change_item_holder, parent, false);
        }

        TextView tvUpdated = (TextView) convertView.findViewById(R.id.tv_updated);
        TextView tvLastChapter = (TextView) convertView.findViewById(R.id.tv_last_chapter);
        TextView tvSourceHost = (TextView) convertView.findViewById(R.id.tv_source_host);

        Source source = (Source) getData().get(position);

        tvUpdated.setText(mContext.getString(R.string.source_change_updated, DateUtils.relativeDate(mContext, source.getUpdated())));
        tvLastChapter.setText(source.getLastChapter());
        tvSourceHost.setText(mContext.getString(R.string.source_change_host, source.getHost()));

        if(getSelectedPosition() == position){
            tvLastChapter.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }else{
            tvLastChapter.setTextColor(mContext.getResources().getColor(R.color.colorChapterOfflined));
        }
        return convertView;
    }


}
