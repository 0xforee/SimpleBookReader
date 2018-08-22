package org.foree.bookreader.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bookinfopage.BookInfoActivity;
import org.foree.bookreader.homepage.BookShelfActivity;
import org.foree.bookreader.update.UpdateAgent;

import java.util.Locale;

/**
 * Created by foree on 16-7-28.
 * 设置界面的内部实现
 */
public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final String KEY_SHOW_ADD_BOOK_ENTRY = "show_add_book_entry";
    private static final int CLICK_NUM = 7;
    private static final int TOAST_NUM = 3;
    private int mClickCount = 0;
    private boolean mShowAddBookEntry = false;
    private Preference mAddEntryPreference;
    private PreferenceCategory mAboutCate;
    private Toast mShowToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_all);

        Preference checkUpdate = findPreference(SettingsActivity.KEY_PREF_CHECK_UPDATE);
        checkUpdate.setSummary(String.format(getString(R.string.update_version_name_title), GlobalConfig.getInstance().getVersionName()));

        // add book preference
        mAddEntryPreference = findPreference(SettingsActivity.KEY_PREF_ADD_BOOK);
        mAboutCate = (PreferenceCategory) findPreference(SettingsActivity.KEY_PREF_CATE_ABOUT);

        mShowAddBookEntry = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(KEY_SHOW_ADD_BOOK_ENTRY, false);
        Log.d(TAG, "[foree] onCreate: show entry = " + mShowAddBookEntry);
        if (!mShowAddBookEntry) {
            mAboutCate.removePreference(mAddEntryPreference);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param preferenceScreen
     * @param preference
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()){
            case SettingsActivity.KEY_PREF_CHECK_UPDATE:
                UpdateAgent.checkUpdate(getActivity(), true);
                break;
            case SettingsActivity.KEY_PREF_SYNC_FREQUENCY:
                mClickCount += 1;
                if (!mShowAddBookEntry && mClickCount >= TOAST_NUM) {
                    if (mClickCount < CLICK_NUM) {
                        if(mShowToast == null){
                            mShowToast = Toast.makeText(getActivity(),
                                    String.format(Locale.CHINA, "还剩余%d次打开隐藏选项", CLICK_NUM - mClickCount), Toast.LENGTH_SHORT);
                        }else{
                            mShowToast.setText(String.format(Locale.CHINA, "还剩余%d次打开隐藏选项", CLICK_NUM - mClickCount));
                        }
                        mShowToast.show();
                        return true;
                    }

                    if (getPreferenceScreen().findPreference(SettingsActivity.KEY_PREF_ADD_BOOK) == null) {
                        mAboutCate.addPreference(mAddEntryPreference);
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(KEY_SHOW_ADD_BOOK_ENTRY, true).apply();
                    }
                }
                break;
            case SettingsActivity.KEY_PREF_ADD_BOOK:
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText editText = new EditText(getActivity());
                builder.setTitle("请输入书籍bookid");
                builder.setView(editText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent bookInfoIntent = new Intent(getActivity(), BookInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("book_url", editText.getText().toString());
                        bundle.putString("source_key", "http://api.zhuishu.com");
                        bookInfoIntent.putExtras(bundle);
                        startActivity(bookInfoIntent);
                    }
                });
                builder.show();
                break;
            case SettingsActivity.KEY_PREF_DISCLAIMER:
                AlertDialog.Builder disclaimer = new AlertDialog.Builder(getActivity());
                disclaimer.setTitle(R.string.pref_key_disclaimer)
                        .setMessage(R.string.pref_message_disclaimer)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                break;
            default:
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
