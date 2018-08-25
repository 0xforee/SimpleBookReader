package org.foree.bookreader.readpage;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foree
 * @date 2018/8/25
 * @description listAdapter的基类，用于sourcechangeList和contentsList
 */
public abstract class AbstractItemListAdapter<T> extends BaseAdapter {
    private int selectedPosition = 0;
    private List<T> data = new ArrayList<>();
    Context mContext;

    public AbstractItemListAdapter(Context context) {
        mContext = context;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public List<T> getData() {
        return data;
    }

    public void updateData(List<T> newData) {
        if(newData != null){
            data.clear();
            data.addAll(newData);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
