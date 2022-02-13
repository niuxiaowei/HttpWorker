package com.mi.http.multibaseurl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/7/9.
 */
public abstract class AbsAddCommonParamsHelper {

    protected int mVersionCode;
    protected String mVersionName;
    protected String mPackName;

    public AbsAddCommonParamsHelper(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            mVersionCode = packageInfo.versionCode;
            mVersionName = packageInfo.versionName;
            mPackName = packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public abstract Request add(Request request);
}
