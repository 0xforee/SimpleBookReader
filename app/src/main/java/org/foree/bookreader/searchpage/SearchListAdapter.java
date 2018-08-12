package org.foree.bookreader.searchpage;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.common.BaseRecyclerAdapter;

import java.util.List;

/**
 * Created by foree on 16-7-22.
 */
public class SearchListAdapter extends BaseRecyclerAdapter<SearchListAdapter.MyViewHolder> {
    private LayoutInflater mLayoutInflater;
    private List<Book> bookList;
    private SparseBooleanArray mSelectedItemsIds;
    private Context mContext;

    public SearchListAdapter(Context context, List<Book> itemList) {
        mLayoutInflater = LayoutInflater.from(context);
        bookList = itemList;
        mSelectedItemsIds = new SparseBooleanArray();
        mContext = context;
    }

    @Override
    public SearchListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.rv_search_holder, parent, false));

    }

    @Override
    public void onBindViewHolder(final SearchListAdapter.MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (bookList != null && !bookList.isEmpty()) {
            holder.tvBookName.setText(bookList.get(position).getBookName());
            holder.tvAuthor.setText(bookList.get(position).getAuthor());
        }

        // 设置选中的背景颜色
        holder.itemView.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4 : Color.TRANSPARENT);

        if (bookList.get(position).getBookCoverUrl() != null) {
            Glide.with(mContext).load(bookList.get(position).getBookCoverUrl()).crossFade().into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return (null != bookList ? bookList.size() : 0);
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    private void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }

        notifyDataSetChanged();
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedItemsIds() {
        return mSelectedItemsIds;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookName, tvAuthor;
        ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            tvBookName = (TextView) view.findViewById(R.id.tv_novel_name);
            imageView = (ImageView) view.findViewById(R.id.iv_novel_image);
            tvAuthor = (TextView) view.findViewById(R.id.tv_novel_author);
        }
    }
}

