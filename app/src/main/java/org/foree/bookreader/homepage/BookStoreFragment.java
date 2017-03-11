package org.foree.bookreader.homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.book.Book;

import java.util.ArrayList;
import java.util.List;

public class BookStoreFragment extends Fragment {
    private ExpandableListView mExpandableListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ExpandableListAdapter mAdapter;
    private List<List<Book>> bookStoreList;
    private List<Book> categoryList;

    public static BookStoreFragment newInstance() {
        BookStoreFragment fragment = new BookStoreFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // generate test data
        bookStoreList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            categoryList = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                Book book = new Book("book" + j, "group" + i);
                categoryList.add(book);
            }
            bookStoreList.add(categoryList);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_store, container, false);

        //mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        //mSwipeRefreshLayout.setOnRefreshListener(this);

        mExpandableListView = (ExpandableListView) view.findViewById(R.id.el_book_store);
        setUpExpandableListView();

        return view;
    }

    private void setUpExpandableListView() {
        mAdapter = new BookStoreExpandableListAdapter(this.getContext(), bookStoreList);
        mExpandableListView.setAdapter(mAdapter);
        for (int i = 0; i < bookStoreList.size(); i++) {
            mExpandableListView.expandGroup(i);
        }
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        mExpandableListView.setGroupIndicator(null);
    }

}
