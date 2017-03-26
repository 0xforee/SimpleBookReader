package org.foree.bookreader.homepage;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.igexin.sdk.PushManager;

import org.foree.bookreader.R;
import org.foree.bookreader.settings.SettingsActivity;

public class BookShelfActivity extends AppCompatActivity {

    private static final String TAG = BookShelfActivity.class.getSimpleName();

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    TabViewPagerAdapter tabViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        initViews();

        PushManager.getInstance().initialize(this.getApplicationContext());

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // Associate searchable configuration with searchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                break;
            case R.id.action_settings:
                Intent intent = new Intent(BookShelfActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }
}
