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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.growingio.android.advert.AdvertConfig;
import com.growingio.android.advert.AdvertLibraryGioModule;
import com.growingio.android.apm.ApmConfig;
import com.growingio.android.apm.ApmLibraryGioModule;
import com.growingio.android.oaid.OaidLibraryGioModule;
import com.growingio.android.protobuf.ProtobufLibraryModule;
import com.growingio.android.sdk.autotrack.CdpAutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.track.events.helper.EventExcludeFilter;
import com.growingio.android.sdk.track.events.helper.FieldIgnoreFilter;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.advert.DeepLinkCallback;
import com.tencent.smtt.sdk.QbSdk;

import java.util.List;
import java.util.Map;

public class DemoApplication extends Application {
    private static final String TAG = "DemoApplication";

    private static boolean sIsAutotracker = true;
    private static CdpAutotrackConfiguration sConfiguration;

    public static void setConfiguration(CdpAutotrackConfiguration configuration) {
        sConfiguration = configuration;
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        super.onCreate();

        //不允许多个进程共享一个 WebView 数据目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName();
            if (!getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }

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

        AdvertConfig advertConfig = new AdvertConfig();
        advertConfig.setDeepLinkCallback(new DeepLinkCallback() {
            @Override
            public void onReceive(Map<String, String> params, int error, long appAwakePassedTime) {
                Log.e(TAG, "DeepLinkCallback");
                Log.e(TAG, String.valueOf(params));
            }
        });
        advertConfig.setDeepLinkHost("https://ads-uat.growingio.cn"); // uat-uba
        advertConfig.setReadClipBoardEnable(true);

        ApmConfig apmConfig = new ApmConfig();
        apmConfig.setActivityLifecycleTracing(true)
                .setFragmentXLifecycleTracing(false)
                .setFragmentSupportLifecycleTracing(true)
                .setFragmentSystemLifecycleTracing(false)
                .setUncaughtException(true)
                .setPrintUncaughtException(true);
        if (sConfiguration == null) {
            sConfiguration = new CdpAutotrackConfiguration("bc675c65b3b0290e", "growing.ff8a70351000af43") //uat-uba
                    .setDataSourceId("8b17366d9e90b05b")
                    .setDataCollectionServerHost("http://117.50.84.75:8080")
                    .setUploadExceptionEnabled(false)
                    .setDebugEnabled(true)
                    .setDataCollectionEnabled(true)
                    .setIdMappingEnabled(true)
                    .setRequireAppProcessesEnabled(true)
                    .setExcludeEvent(EventExcludeFilter.of(EventExcludeFilter.REENGAGE))
                    .setIgnoreField(FieldIgnoreFilter.of(FieldIgnoreFilter.FIELD_IGNORE_ALL))
                    //.addConfiguration(oaidConfig)
                    .addPreloadComponent(new AdvertLibraryGioModule(), advertConfig)
                    .addPreloadComponent(new ApmLibraryGioModule(), apmConfig)
                    .addPreloadComponent(new ProtobufLibraryModule())
//                    .addPreloadComponent(new EncoderLibraryGioModule())
                    .addPreloadComponent(new OaidLibraryGioModule());
        }

        enableStrictMode();

        long startTime = System.currentTimeMillis();
        GrowingAutotracker.startWithConfiguration(this, sConfiguration);
        Log.d(TAG, "start time: " + (System.currentTimeMillis() - startTime));

    }

    private boolean isMainProcess() {
        @SuppressLint("WrongConstant") ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
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

    private void enableStrictMode() {
        StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .detectCustomSlowCalls()
                .permitDiskReads()
                .permitDiskWrites()
                .penaltyLog();
        StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .penaltyLog();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            threadPolicyBuilder.detectResourceMismatches();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            threadPolicyBuilder.detectUnbufferedIo();
            vmPolicyBuilder.detectContentUriWithoutPermission();
        }

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }
}
