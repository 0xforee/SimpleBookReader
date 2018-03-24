package org.foree.bookreader.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.searchpage.SearchResultsActivity;
import org.foree.bookreader.settings.SettingsActivity;

public class BookShelfActivity extends BaseActivity {

    private static final String TAG = BookShelfActivity.class.getSimpleName();

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    TabViewPagerAdapter tabViewPagerAdapter;

    private boolean mNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        initViews();

        mNightMode = GlobalConfig.getInstance().isNightMode();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
        if (mNightMode != GlobalConfig.getInstance().isNightMode()) {
            GlobalConfig.getInstance().changeTheme();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 100);
        }
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookShelfActivity.this, SearchResultsActivity.class);
                startActivity(intent);
            }
        });

        viewPager = (ViewPager) findViewById(R.id.view_pager_main);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_main);

        tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(tabViewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                break;
            case R.id.action_settings:
                Intent intent = new Intent(BookShelfActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
