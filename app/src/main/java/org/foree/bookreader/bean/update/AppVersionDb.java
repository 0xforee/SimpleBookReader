package org.foree.bookreader.bean.update;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * @author foree
 * @date 2018/8/5
 * @description 版本更新对应的云端数据表单
 */
public class AppVersionDb extends BmobObject {
    private Integer versionCode;
    private String versionName;
    private String updateLog;
    private String apkPath;
    private BmobFile apkFile;
    private Integer apkSize;

    public Integer getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public String getApkPath() {
        return apkPath;
    }

    public Integer getApkSize() {
        return apkSize;
    }

    @Override
    public String toString() {
        return "AppVersionDb{" +
                "versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", updateLog='" + updateLog + '\'' +
                ", apkPath='" + apkPath + '\'' +
                ", apkFile=" + apkFile +
                ", apkSize=" + apkSize +
                '}';
    }

    public BmobFile getApkFile() {
        return apkFile;
    }

}
