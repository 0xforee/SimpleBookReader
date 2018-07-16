package org.foree.bookreader.readpage;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.foree.bookreader.R;

import java.util.List;
import java.util.Map;

/**
 * @author foree
 * @date 2018/7/16
 * @description
 */
public class CustomSourceListAdapter extends SimpleAdapter{
    private int mSelectedPosition = 0;
    private Context mContext;
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public CustomSourceListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
    }

    /**
     * @param position
     * @param convertView
     * @param parent
     * @see Adapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root =  super.getView(position, convertView, parent);
        TextView title = (TextView) root.findViewById(R.id.tv_last_chapter);
        if(mSelectedPosition == position){
            title.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }else{
            title.setTextColor(mContext.getResources().getColor(R.color.colorChapterOfflined));
        }
        return root;
    }

    public void setSelectedPosition(int selection){
        mSelectedPosition = selection;
    }
}
