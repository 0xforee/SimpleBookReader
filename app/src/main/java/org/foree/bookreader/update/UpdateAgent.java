package org.foree.bookreader.update;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.update.AppVersionDb;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;

/**
 * @author foree
 * @date 2018/8/5
 * @description 执行更新逻辑
 */
public class UpdateAgent {
    private static final String TAG = UpdateAgent.class.getSimpleName();

    public static void checkUpdate(final Context context, final boolean force) {
        BmobQuery<AppVersionDb> query = new BmobQuery<>();
        query.order("-versionCode").setLimit(1);
        query.findObjects(new FindListener<AppVersionDb>() {
            @Override
            public void done(List<AppVersionDb> list, BmobException e) {
                if (e == null && !list.isEmpty()) {
                    // get newest query
                    AppVersionDb appVersionDb = list.get(0);
                    Log.d(TAG, "[foree] done: appVersionDb = " + appVersionDb);

                    // release file
                    BmobFile apkFile = appVersionDb.getApkFile();
                    final File saveFile = new File(context.getExternalCacheDir(), apkFile.getFilename());

                    // check if new version release
                    int curVersionCode = GlobalConfig.getInstance().getVersionCode();
                    if (newCodeReleased(curVersionCode, appVersionDb.getVersionCode())) {
                        if (!skipUpdate(appVersionDb.getVersionCode()) || force) {
                            createDialog(context, force, saveFile, appVersionDb);
                        }
                    } else if (force) {
                        Toast.makeText(context, R.string.update_already_new, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "bmob query done: error", e);
                }
            }
        });
    }

    private static boolean newCodeReleased(int curVersionCode, int newVersionCode) {
        return newVersionCode > curVersionCode;
    }

    private static Intent getInstallIntent(String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    private static boolean skipUpdate(int newVersionCode) {
        // get preference
        return GlobalConfig.getInstance().skipUpdate(newVersionCode);
    }

    private static void ignoreVersion(int newVersionCode) {
        GlobalConfig.getInstance().ignoreVersion(newVersionCode);
    }

    private static View getDialogContentView(Context context, AppVersionDb appVersionDb) {
        View dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_update_content, null, false);
        TextView versionName = (TextView) dialogContent.findViewById(R.id.tv_update_version_name);
        TextView size = (TextView) dialogContent.findViewById(R.id.tv_update_apk_size);
        TextView content = (TextView) dialogContent.findViewById(R.id.tv_update_content);

        versionName.setText(String.format(content.getResources().getString(R.string.update_version_name_title), appVersionDb.getVersionName()));
        float apkSize = ((float) appVersionDb.getApkSize()) / 1024 / 1024;
        size.setText(String.format(content.getResources().getString(R.string.update_size), apkSize));
        content.setText(appVersionDb.getUpdateLog().replace(";", "\n"));

        return dialogContent;
    }

    private static void createDialog(final Context context, boolean force, final File saveFile, final AppVersionDb appVersionDb) {

        final boolean fileExits = saveFile.exists();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.update_discover_new);

        builder.setView(getDialogContentView(context, appVersionDb));
        builder.setNegativeButton(R.string.update_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (!fileExits) {
            builder.setNeutralButton(R.string.update_ignore, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ignoreVersion(appVersionDb.getVersionCode());
                }
            });
        }
        builder.setPositiveButton(fileExits ? R.string.update_install : R.string.update_download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fileExits) {
                    context.startActivity(getInstallIntent(saveFile.getAbsolutePath()));
                } else {
                    // create notification
                    final Notification.Builder noBuilder = new Notification.Builder(context);
                    noBuilder.setContentTitle(context.getString(R.string.update_notification_title));
                    noBuilder.setContentText(context.getString(R.string.update_notification_text));
                    noBuilder.setSmallIcon(R.drawable.ic_launcher);
                    noBuilder.setProgress(100, 0, false);
                    final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(1, noBuilder.build());

                    appVersionDb.getApkFile().download(saveFile, new DownloadFileListener() {
                        int process = 0;

                        @Override
                        public void done(String s, BmobException e) {
                            if (e != null) {
                                Log.e(TAG, "done: download error", e);
                                noBuilder.setContentText(context.getString(R.string.update_notification_download_error));
                            } else {
                                Log.d(TAG, "[foree] done: download complete");
                                noBuilder.setContentText(context.getString(R.string.update_notification_download_complete));
                                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                        getInstallIntent(saveFile.getAbsolutePath()), PendingIntent.FLAG_UPDATE_CURRENT);
                                noBuilder.setContentIntent(pendingIntent);
                                noBuilder.setAutoCancel(true);

                            }
                            nm.notify(1, noBuilder.build());
                        }

                        @Override
                        public void onProgress(Integer integer, long l) {
                            if (integer > process) {
                                process += 5;
                                noBuilder.setProgress(100, process, false);
                                nm.notify(1, noBuilder.build());
                            }
                        }
                    });
                }
            }
        });

        builder.create().show();
    }

}
