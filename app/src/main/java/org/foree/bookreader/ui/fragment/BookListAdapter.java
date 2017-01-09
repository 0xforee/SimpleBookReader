package org.foree.bookreader.ui.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.foree.bookreader.R;

import org.foree.bookreader.book.Book;

import java.util.List;

/**
 * Created by foree on 16-7-22.
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.MyViewHolder>{
    private LayoutInflater mLayoutInflater;
    private List<Book> bookList;

    public BookListAdapter(Context context, List<Book> itemList){
        mLayoutInflater = LayoutInflater.from(context);
        bookList = itemList;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
    {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public BookListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mLayoutInflater.inflate(R.layout.item_list_holder, parent, false));

        return holder;
    }

    @Override
    public void onBindViewHolder(final BookListAdapter.MyViewHolder holder, int position) {
        if( bookList != null && !bookList.isEmpty())
            holder.tvBookName.setText(bookList.get(position).getBook_name());

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvBookName;

        public MyViewHolder(View view){
            super(view);
            tvBookName = (TextView)view.findViewById(R.id.tv_item_title);
        }
    }
}

