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

package com.growingio.android.sdk.track.utils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.growingio.android.sdk.track.utils.rom.RomChecker;

import java.util.List;
import java.util.Map;

public final class ContextUtil {

    private static final String TAG = "GIO.ContextUtil";

    private ContextUtil() {
    }

    /**
     * 对应于context.registerReceiver(receiver, intentFilter);
     * 华为EMUI对BroadcastReceiver数量限制在500一下,  未防止用户泄露broadcast crash在SDK端，
     * 将自身放置在EMUI的白名单中
     */
    public static void registerReceiver(@NonNull Context context, BroadcastReceiver receiver, IntentFilter intentFilter) {
        registerReceiver(context, receiver, intentFilter, true);
    }

    private static void registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter, boolean needCheckEMUI) {
        try {
            context.registerReceiver(receiver, filter);
        } catch (Throwable throwable) {
            if (needCheckEMUI
                    && RomChecker.isHuaweiRom()) {
                boolean addWhiteListOk = eMUIAddWhiteList(context);
                if (addWhiteListOk) {
                    LogUtil.d(TAG, "华为: add to WhiteList Success");
                    registerReceiver(context, receiver, filter, false);
                } else {
                    LogUtil.e(TAG, "华为: add to WhiteList Failed");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean eMUIAddWhiteList(Context context) {
        try {
            Application application = (Application) context.getApplicationContext();
            Object loadedApk = ReflectUtil.findField(Application.class, application, "mLoadedApk");
            if (loadedApk == null)
                return false;
            Object receiverResource = ReflectUtil.findFieldRecur(loadedApk, "mReceiverResource");
            if (receiverResource == null)
                return false;
            List mWhiteList = ReflectUtil.findFieldRecur(receiverResource, "mWhiteList");
            if (mWhiteList == null) {
                // for Android 8.0以上
                Map whiteListMap = ReflectUtil.findFieldRecur(receiverResource, "mWhiteListMap");
                if (whiteListMap == null)
                    return false;
                Object firstItem = whiteListMap.get("0");
                if (firstItem instanceof List)
                    mWhiteList = (List) firstItem;
                else {
                    return false;
                }
            }
            String packageName = context.getPackageName();
            if (mWhiteList.contains(packageName)) {
                return false;
            }
            mWhiteList.add(packageName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
