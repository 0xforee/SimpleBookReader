package org.foree.bookreader.readpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.book.Chapter;

import java.util.ArrayList;
import java.util.List;

public class CustomChapterListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Chapter> mChapterList;
    private int mSelectedPosition;

    public CustomChapterListAdapter(Context context) {
        mContext = context;
        mChapterList = new ArrayList<>();
        mSelectedPosition = 0;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mChapterList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mChapterList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.lv_content_item_holder, parent, false);
        }

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_chapter_title);
        Chapter chapter = mChapterList.get(position);
        // set view
        tvTitle.setText(chapter.getChapterTitle());

        if (chapter.isOffline()) {
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorChapterOfflined));
        } else {
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorChapterUnlined));

        }

        // set current position color
        if (mSelectedPosition == position) {
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
        return convertView;
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
    }

    public void updateData(List<Chapter> chapters){
        if ( chapters != null && chapters.size() != 0){
            mChapterList.clear();
            mChapterList.addAll(chapters);
        }
    }

    public void clear(){
        mChapterList.clear();
    }
}
