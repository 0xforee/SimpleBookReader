package org.foree.bookreader.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;

/**
 * Created by foree on 17-6-21.
 */

public class AboutActivity extends BaseActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
