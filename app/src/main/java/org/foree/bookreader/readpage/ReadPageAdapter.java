package org.foree.bookreader.readpage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import org.foree.bookreader.bean.book.Chapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author foree
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ReadPageAdapter extends FragmentPagerAdapter {
    private static final String TAG = "ReadPageAdapter";
    private static final boolean DEBUG = false;

    private ReadViewPager mViewPager;
    private ReadFragment[] fragments;
    private ChapterLinkedList<ReadPageDataSet> mChapterList;
    private Map<Integer, Chapter> mUnlinkedData = new HashMap<>();
    private String mUserExpectedUrl = null;
    private BookRecord mBookRecord;
    private ArrayList<Callback> mCallbacks;

    public ReadPageAdapter(FragmentManager fm, ViewPager viewPager, BookRecord bookRecord) {
        super(fm);
        mViewPager = (ReadViewPager) viewPager;
        fragments = new ReadFragment[]{
                ReadFragment.newInstance(),
                ReadFragment.newInstance(),
                ReadFragment.newInstance()
        };

        mChapterList = new ChapterLinkedList<>();
        mBookRecord = bookRecord;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return fragments[position];
    }


    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
        if (mDataChanged) {
            onDataChanged(mPosition);
            mDataChanged = false;
        }
    }

    private boolean mDataChanged = true;
    private int mPosition = 0;

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        Log.d(TAG, "[foree] setPrimaryItem: position = " + position + ", object = " + object);

        if (position != 1) {
            mPosition = position;
            mDataChanged = true;
            mViewPager.setCurrentItem(1, false);
        }
    }

    public void initChapter(String chapterUrl) {
        mUserExpectedUrl = chapterUrl;
        checkCacheMap();

        // move head
        Iterator iterator = mChapterList.iterator();
        while (iterator.hasNext()) {
            ReadPageDataSet i = (ReadPageDataSet) iterator.next();
            if (i.getUrl().equals(chapterUrl)) {
                mChapterList.moveTo(i);
                break;
            }
        }

        updateContent();

    }

    public void initChapter(String chapterUrl, int position) {
        mUserExpectedUrl = chapterUrl;
        checkCacheMap();

        // move head
        Iterator iterator = mChapterList.iterator();
        while (iterator.hasNext()) {
            ReadPageDataSet i = (ReadPageDataSet) iterator.next();
            if (i.getUrl().equals(chapterUrl) && position == i.getIndex()) {
                mChapterList.moveTo(i);
            }
        }

        updateContent();
    }

    // -3是zuo边缘, 5是you边缘
    private boolean mInitial = true;

    private void onDataChanged(int position) {
        int offset = position - 1;

        if (mInitial) {
            mInitial = false;
        } else {
            if (offset < 0) {
                mChapterList.movePrev();
                if (DEBUG) Log.d(TAG, "[foree] onDataChanged: 向左滑动");
            } else if (offset > 0) {
                mChapterList.moveNext();
                if (DEBUG) Log.d(TAG, "[foree] onDataChanged: 向右滑动");
            }
            checkIfChapterSwitched();
        }

        updateContent();

    }

    private void updateContent() {
        // 上一页已经无法获取，当前页到了最左边，禁止左滑动
        mViewPager.setPreScrollDisable(!mChapterList.hasPrevious());

        // 下一页已经无法获取，当前页到了最左边，禁止左滑动
        mViewPager.setPostScrollDisable(!mChapterList.hasNext());

        // set previous
        if (mChapterList.hasPrevious()) {
            fragments[0].setData(mChapterList.getPrevData());
        }

        // set current
        fragments[1].setData(mChapterList.getCurrentData());

        // set next
        if (mChapterList.hasNext()) {
            fragments[2].setData(mChapterList.getNextData());
        }

    }

    public int getCurrentPageIndex(){
        if(mChapterList.getCurrentData() != null) {
            return mChapterList.getCurrentData().getIndex();
        }
        return 0;
    }

    /**
     * 章节链表需要按顺序添加，且第一个添加的章节一定是要用户指定的初始化章节
     * 如果不满足加到缓存中，在每次调用时检查缓存是否可以添加到章节链表中
     *
     * @param chapter
     */
    public void addChapter(Chapter chapter) {
        int chapterIndex = mBookRecord.getChapterIndex(chapter.getChapterUrl());
        mUnlinkedData.put(chapterIndex, chapter);
        checkCacheMap();
        updateContent();
    }

    /**
     * 添加满足要求的数据到章节链表
     */
    private void checkCacheMap() {
        // init之后开始更新list
        if (mUserExpectedUrl != null) {
            int chapterIndex = mBookRecord.getChapterIndex(mUserExpectedUrl);
            if (mChapterList.getFirstData() == null && mUnlinkedData.containsKey(chapterIndex)) {
                addNextChapter(mUnlinkedData.get(chapterIndex));
                mUnlinkedData.remove(chapterIndex);
            }
            // 从缓存中添加其他章节
            int firstIndex = mBookRecord.getChapterIndex(mChapterList.getFirstData().getUrl());
            int lastIndex = mBookRecord.getChapterIndex(mChapterList.getLastData().getUrl());

            int maybePrev = firstIndex - 1;
            int maybeLast = lastIndex + 1;

            while (mUnlinkedData.containsKey(maybePrev)) {
                addPreChapter(mUnlinkedData.get(maybePrev));
                mUnlinkedData.remove(maybePrev);
                maybePrev -= 1;
            }
            while (mUnlinkedData.containsKey(maybeLast)) {
                addNextChapter(mUnlinkedData.get(maybeLast));
                mUnlinkedData.remove(maybeLast);
                maybeLast += 1;
            }
        }
    }

    /**
     * 添加前一章到list中，倒序添加
     */
    private void addPreChapter(Chapter chapter) {
        int size = chapter.numberOfPages();
        for (int i = size - 1; i > 0; i--) {
            ReadPageDataSet data = new ReadPageDataSet(
                    chapter.getChapterUrl(), chapter.getChapterTitle(), chapter.getPage(i), size, i, mBatteryLevel);

            mChapterList.addFirst(data);
        }
        if (DEBUG) Log.d(TAG, "addPreChapter() called with: chapter = [" + chapter + "]");
    }

    /**
     * 添加后一章到list中，正序添加
     */
    private void addNextChapter(Chapter chapter) {
        int size = chapter.numberOfPages();
        for (int i = 0; i < size; i++) {
            ReadPageDataSet data = new ReadPageDataSet(
                    chapter.getChapterUrl(), chapter.getChapterTitle(), chapter.getPage(i), size, i, mBatteryLevel);

            mChapterList.addLast(data);
        }
        if (DEBUG) Log.d(TAG, "addNextChapter() called with: chapter = [" + chapter + "]");
    }

    public void reset() {
        mChapterList.reset();
        mUserExpectedUrl = null;
    }

    private void checkIfChapterSwitched() {
        String curChapterUrl = mChapterList.getCurrentData().getUrl();
        if (!mUserExpectedUrl.equals(curChapterUrl)) {
            mUserExpectedUrl = curChapterUrl;
            notifyChapterSwitched(curChapterUrl);
        }
    }

    private void notifyChapterSwitched(String newChapterUrl) {
        if (mCallbacks != null && mCallbacks.size() != 0) {
            for (Callback callback : mCallbacks) {
                callback.onChapterSwitched(newChapterUrl);
            }
        }
    }

    public void registerCallback(Callback callback) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(Callback callback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
        }
    }

    interface Callback {
        /**
         * 在章节切换的时候触发，方便监听者做出处理
         *
         * @param newChapterUrl 切换到的新章节Url
         */
        void onChapterSwitched(String newChapterUrl);
    }

    private int mBatteryLevel = 0;
    public void updateBatteryLevel(int level){
        mBatteryLevel = level;
    }

}
