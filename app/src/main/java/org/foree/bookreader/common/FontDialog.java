package org.foree.bookreader.common;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.readpage.ReadActivity;
import org.foree.bookreader.settings.SettingsActivity;

/**
 * Created by foree on 17-4-7.
 */

public class FontDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = FontDialog.class.getSimpleName();

    private static final float BRIGHTNESS_MAX = 255f;
    Window window;
    private float mBrightness;
    private LinearLayout rootView;
    private SeekBar seekBar;
    private ReadActivity activity;
    private RadioGroup fontCustomBg;
    private String[] mFontBgArray;

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
        int color = GlobalConfig.getInstance().getPageBackground();
        for (int i = 0; i < fontCustomBg.getChildCount(); i++) {
            RadioButton child = (RadioButton) fontCustomBg.getChildAt(i);
            int bg = (int) child.getTag();
            if (bg == color) {
                child.setChecked(true);
            }
        }
    }

    private void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        rootView = (LinearLayout) layoutInflater.inflate(R.layout.dialog_font_layout, null);
        fontCustomBg = (RadioGroup) rootView.findViewById(R.id.font_custom_bg);

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

        mFontBgArray = getContext().getResources().getStringArray(R.array.read_background_array);
        String[] fontBgCheckedArray = getContext().getResources().getStringArray(R.array.read_background_checked_array);
        String[] fontBgTextArray = getContext().getResources().getStringArray(R.array.read_background_text_array);

        for (int i = 0; i < mFontBgArray.length; i++) {
            RadioButton radioButton = (RadioButton) layoutInflater.inflate(R.layout.dialog_font_radiobutton_layout, fontCustomBg, false);
            radioButton.setBackground(getDrawList(Color.parseColor(mFontBgArray[i]), Color.parseColor(fontBgCheckedArray[i])));
            radioButton.setId(i);
            radioButton.setText(fontBgTextArray[i]);
            radioButton.setTag(Color.parseColor(mFontBgArray[i]));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int bg = (int) view.getTag();
                    GlobalConfig.getInstance().setPageBackground(bg);
                    rootView.setBackgroundColor(bg);
                }
            });

            fontCustomBg.addView(radioButton);
        }

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
        checkedDrawable.setColor(backgroundColor);
        checkedDrawable.setShape(GradientDrawable.RECTANGLE);
        checkedDrawable.setCornerRadius(5);
        checkedDrawable.setStroke(7, primaryColor);

        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setColor(backgroundColor);
        normalDrawable.setShape(GradientDrawable.RECTANGLE);
        normalDrawable.setCornerRadius(5);

        bg.addState(new int[]{android.R.attr.state_checked}, checkedDrawable);
        bg.addState(new int[]{}, normalDrawable);

        return bg;

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
