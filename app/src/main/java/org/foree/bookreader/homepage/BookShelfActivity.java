package org.foree.bookreader.homepage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.service.SyncService;
import org.foree.bookreader.settings.SettingsActivity;

public class BookShelfActivity extends AppCompatActivity {

    private static final String TAG = BookShelfActivity.class.getSimpleName();
    private static final String KEY_RECREATE = TAG + "_recreate";

    public static final boolean DEBUG = false;

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    TabViewPagerAdapter tabViewPagerAdapter;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private boolean mNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        initViews();

        mNightMode = GlobalConfig.getInstance().isNightMode();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SyncService.class);
        alarmIntent = PendingIntent.getService(this, 0, intent, 0);

        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_RECREATE)) {
            Log.d(TAG, "onCreate: recreate activity");
        } else {
            // loading
            alarmManager.cancel(alarmIntent);
            Log.d(TAG, "onCreate: cancel alarm");
        }

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
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RECREATE, true);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNightMode == GlobalConfig.getInstance().isNightMode()) {
            if (DEBUG) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 10000,
                        60000, alarmIntent);
            } else {

                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        getInterval(), alarmIntent);
            }
            Log.d(TAG, "onDestroy: start alarm");
        }
    }

    private long getInterval() {
        int select = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_PREF_SYNC_FREQUENCY, "60"));

        return select * 60 * 1000;
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
