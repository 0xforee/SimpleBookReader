package org.foree.bookreader.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.searchpage.SearchResultsActivity;
import org.foree.bookreader.settings.SettingsActivity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BookShelfActivity extends BaseActivity {

    private static final String TAG = BookShelfActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

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

        // fade in toolbar
        fadeInToolbar();

        // change nightmode
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
        toolbar.setNavigationIcon(R.drawable.ic_action_search);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showKeyboard();

                transitionToSearch();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.view_pager_main);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_main);

        tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(tabViewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);

    }

    private void transitionTabFadeOut() {
        Transition transition = new AutoTransition();
    }

    private void transitionToSearch() {
        Transition transition = new AutoTransition();
        transition.setDuration(250);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                Intent intent = new Intent(BookShelfActivity.this, SearchResultsActivity.class);
                startActivity(intent);

                overridePendingTransition(0, 0);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(toolbar, transition);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        layoutParams.height = (int) getResources().getDimension(R.dimen.toolbar_height_large);
        layoutParams.setMargins(0, 0, 0, 0);
        toolbar.setLayoutParams(layoutParams);
        hideToolBarContent();

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

    private void fadeInToolbar() {
        Transition transition = new AutoTransition();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(toolbar, transition);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        int toolbarMargin = (int) getResources().getDimension(R.dimen.toolbar_margin);
        layoutParams.setMargins(toolbarMargin, toolbarMargin, toolbarMargin, toolbarMargin);
        layoutParams.height = (int) getResources().getDimension(R.dimen.toolbar_height_small);
        toolbar.setLayoutParams(layoutParams);

        showToolBarContent();

    }

    private void hideToolBarContent() {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            toolbar.getChildAt(i).setVisibility(View.GONE);
        }
    }

    private void showToolBarContent() {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            toolbar.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }


    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }
}
