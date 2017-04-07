package org.foree.bookreader.common;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;

import org.foree.bookreader.R;
import org.foree.bookreader.settings.SettingsActivity;

/**
 * Created by foree on 17-4-7.
 */

public class FontDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = FontDialog.class.getSimpleName();

    private SharedPreferences backgroundPreference;

    private RadioButton classicalRb, dayRb, eyeModeRb, nightRb;

    public FontDialog(Context context) {
        this(context, R.style.fontDialogStyle);
    }

    private FontDialog(Context context, int themeResId) {
        super(context, themeResId);

        init();
        initPreference();
    }

    private void initPreference() {
        backgroundPreference = PreferenceManager.getDefaultSharedPreferences(getContext());

        int index = backgroundPreference.getInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 0);
        switch (index) {
            case 0:
                dayRb.setChecked(true);
                break;
            case 1:
                classicalRb.setChecked(true);
                break;
            case 2:
                eyeModeRb.setChecked(true);
                break;
            case 3:
                nightRb.setChecked(true);
                break;
        }
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_font_layout, null);

        DisplayMetrics dp = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dp);

        setContentView(rootView);

        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.BOTTOM);

            lp.x = 0;
            lp.y = 0;
            lp.width = dp.widthPixels;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            dialogWindow.setAttributes(lp);
        }

        setCanceledOnTouchOutside(true);

        dayRb = (RadioButton) rootView.findViewById(R.id.rb_normal);
        dayRb.setBackground(getDrawList(R.color.day_page_background));
        dayRb.setOnClickListener(this);

        classicalRb = (RadioButton) rootView.findViewById(R.id.rb_classical);
        classicalRb.setBackground(getDrawList(R.color.classical_page_background));
        classicalRb.setOnClickListener(this);

        eyeModeRb = (RadioButton) rootView.findViewById(R.id.rb_eye);
        eyeModeRb.setBackground(getDrawList(R.color.eye_mode_page_background));
        eyeModeRb.setOnClickListener(this);

        nightRb = (RadioButton) rootView.findViewById(R.id.rb_night);
        nightRb.setBackground(getDrawList(R.color.night_page_background));
        nightRb.setOnClickListener(this);
    }


    private StateListDrawable getDrawList(int backgroundColor) {
        StateListDrawable bg = new StateListDrawable();

        GradientDrawable checkedDrawable = new GradientDrawable();
        checkedDrawable.setColor(getContext().getResources().getColor(backgroundColor));
        checkedDrawable.setShape(GradientDrawable.RECTANGLE);
        checkedDrawable.setCornerRadius(5);
        checkedDrawable.setStroke(3, getContext().getResources().getColor(R.color.md_yellow_500));

        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setColor(getContext().getResources().getColor(backgroundColor));
        normalDrawable.setShape(GradientDrawable.RECTANGLE);
        normalDrawable.setCornerRadius(5);

        bg.addState(new int[]{android.R.attr.state_checked}, checkedDrawable);
        bg.addState(new int[]{}, normalDrawable);

        return bg;

    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        switch (view.getId()) {
            case R.id.rb_normal:
                backgroundPreference.edit().putInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 0).apply();
                break;
            case R.id.rb_classical:
                backgroundPreference.edit().putInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 1).apply();
                break;
            case R.id.rb_eye:
                backgroundPreference.edit().putInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 2).apply();
                break;
            case R.id.rb_night:
                backgroundPreference.edit().putInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 3).apply();
                break;
        }
    }
}
