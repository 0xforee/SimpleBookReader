package org.foree.zetianji.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.foree.zetianji.R;
import org.foree.zetianji.service.RefreshService;

public class BookShelfActivity extends AppCompatActivity implements CardView.OnClickListener, RefreshService.StreamCallBack{

    private RefreshService.MyBinder mBinder;
    private RefreshService mRefreshService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();

    CardView cardView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardView = (CardView) findViewById(R.id.novel_card);
        cardView.setOnClickListener(this);

        // start refresh service
        Intent intent = new Intent(this, RefreshService.class);
        startService(intent);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(BookShelfActivity.this, ChapterListActivity.class);
        startActivity(intent);
    }

    @Override
    public void notifyUpdate() {

    }

    private class MyServiceConnection implements ServiceConnection {
        private final String TAG = MyServiceConnection.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mBinder = (RefreshService.MyBinder) iBinder;
            mRefreshService = mBinder.getService();
            mRefreshService.registerCallBack(BookShelfActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

        }
    }
}
