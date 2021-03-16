/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gio.test.three;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.log.Logger;
import com.tencent.smtt.sdk.QbSdk;

import java.util.List;

public class DemoApplication extends Application {
    private static final String TAG = "DemoApplication";

    private static boolean sIsAutotracker = true;
    private static AutotrackConfiguration sConfiguration;

    public static void setIsAutotracker(boolean isAutotracker) {
        sIsAutotracker = isAutotracker;
    }

    public static void setConfiguration(AutotrackConfiguration configuration) {
        sConfiguration = configuration;
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        super.onCreate();

        if (isMainProcess()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
            QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
                @Override
                public void onCoreInitFinished() {
                    Log.e(TAG, "onCoreInitFinished: ");
                }

                @Override
                public void onViewInitFinished(boolean b) {
                    Log.e(TAG, "onViewInitFinished: " + b);
                }
            });
//            startService(new Intent(this, OtherProcessService.class));
        }

        if (sConfiguration == null) {
            sConfiguration = new AutotrackConfiguration("bfc5d6a3693a110d", "growing.98ea5d6021ea4d58")
                    .setUploadExceptionEnabled(false)
                    .setDebugEnabled(true)
                    .setOaidEnabled(false);
        }
        if (sIsAutotracker) {
            GrowingAutotracker.startWithConfiguration(this, sConfiguration);
        } else {
            GrowingTracker.startWithConfiguration(this, sConfiguration);
        }
    }

    private boolean isMainProcess() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();

        if (processInfos == null) {
            Logger.e(TAG, "isMainProcess: RunningAppProcessInfo list is NULL");
            return false;
        }
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
