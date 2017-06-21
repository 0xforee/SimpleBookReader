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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.readpage.ReadActivity;
import org.foree.bookreader.settings.SettingsActivity;

/**
 * Created by foree on 17-4-7.
 */

public class FontDialog extends Dialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = FontDialog.class.getSimpleName();

    private static final float BRIGHTNESS_MAX = 255f;
    Window window;
    private float mBrightness;
    private SharedPreferences backgroundPreference;
    private RadioButton classicalRb, normalRb, eyeModeRb, nightRb;
    private View rootView;
    private SeekBar seekBar;
    private ReadActivity activity;

    private FontDialog(Context context) {
        this(context, R.style.fontDialogStyle);
    }

    private FontDialog(Context context, int themeResId) {
        super(context, themeResId);

        activity = (ReadActivity) context;
        window = getWindow();

        init();
        initPreference();
    }

    private void initPreference() {
        backgroundPreference = PreferenceManager.getDefaultSharedPreferences(getContext());

        int index = backgroundPreference.getInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 0);
        switch (index) {
            case 0:
                normalRb.setChecked(true);
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
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_font_layout, null);

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

        normalRb = (RadioButton) rootView.findViewById(R.id.rb_normal);
        normalRb.setBackground(getDrawList(R.color.normal_page_background, R.color.normal_primary));
        normalRb.setOnClickListener(this);

        classicalRb = (RadioButton) rootView.findViewById(R.id.rb_classical);
        classicalRb.setBackground(getDrawList(R.color.classical_page_background, R.color.classical_primary));
        classicalRb.setOnClickListener(this);

        eyeModeRb = (RadioButton) rootView.findViewById(R.id.rb_eye);
        eyeModeRb.setBackground(getDrawList(R.color.eye_mode_page_background, R.color.eye_primary));
        eyeModeRb.setOnClickListener(this);

        nightRb = (RadioButton) rootView.findViewById(R.id.rb_night);
        nightRb.setBackground(getDrawList(R.color.night_page_background, R.color.night_primary));
        nightRb.setOnClickListener(this);

        ImageView brightness_low = (ImageView) rootView.findViewById(R.id.brightness_low);
        brightness_low.setAlpha(140);
        ImageView brightness_high = (ImageView) rootView.findViewById(R.id.brightness_high);
        brightness_high.setAlpha(140);

        // init brightness
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax((int) BRIGHTNESS_MAX);

    }

    @Override
    public void dismiss() {
        super.dismiss();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = GlobalConfig.getInstance().getAppBrightness();
        activity.getWindow().setAttributes(lp);
    }

    private void initBrightness() {
        mBrightness = GlobalConfig.getInstance().getAppBrightness();

        seekBar.setProgress((int) (mBrightness * BRIGHTNESS_MAX));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = mBrightness;
        window.setAttributes(lp);
    }

    private StateListDrawable getDrawList(int backgroundColor, int primaryColor) {
        StateListDrawable bg = new StateListDrawable();

        GradientDrawable checkedDrawable = new GradientDrawable();
        checkedDrawable.setColor(getContext().getResources().getColor(backgroundColor));
        checkedDrawable.setShape(GradientDrawable.RECTANGLE);
        checkedDrawable.setCornerRadius(5);
        checkedDrawable.setStroke(7, getContext().getResources().getColor(primaryColor));

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
        int index = 0;
        switch (view.getId()) {
            case R.id.rb_normal:
                index = 0;
                break;
            case R.id.rb_classical:
                index = 1;
                break;
            case R.id.rb_eye:
                index = 2;
                break;
            case R.id.rb_night:
                index = 3;
                break;
        }

        backgroundPreference.edit().putInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, index).apply();
        rootView.setBackgroundColor(GlobalConfig.getInstance().getPageBackground());

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mBrightness = progress / BRIGHTNESS_MAX;
            WindowManager.LayoutParams lp = window.getAttributes();
            Log.d(TAG, "onProgressChanged: progress = " + mBrightness);
            lp.screenBrightness = mBrightness;
            window.setAttributes(lp);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        GlobalConfig.getInstance().setAppBrightness(mBrightness);
    }

    public static class Builder {
        private Context mContext;
        private int mThemeResId;
        private FontDialog fontDialog;

        public Builder(Context context) {
            this(context, R.style.fontDialogStyle);
        }

        private Builder(Context context, int themeResId) {
            mContext = context;
            mThemeResId = themeResId;
        }

        public void showDialog() {
            if (fontDialog == null)
                fontDialog = new FontDialog(mContext, mThemeResId);
            fontDialog.rootView.setBackgroundColor(GlobalConfig.getInstance().getPageBackground());
            fontDialog.setContentView(fontDialog.rootView);
            fontDialog.initBrightness();
            fontDialog.show();
        }
    }
}
