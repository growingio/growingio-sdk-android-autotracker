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

package com.growingio.android.sdk.collection;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.interfaces.IGrowingIO;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.utils.JsonUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @deprecated {@link GrowingAutotracker )}
 */
@Deprecated
public class GrowingIO implements IGrowingIO {
    private static final String TAG = "GrowingIO";
    private static final String KEEP_ID = "KEEP_ID";

    private static class SingleInstance {
        private static final IGrowingIO INSTANCE = new GrowingIO();
    }

    private GrowingIO() {
    }

    /**
     * @deprecated {@link GrowingAutotracker#get()}
     */
    @Deprecated
    public static IGrowingIO getInstance() {
        return SingleInstance.INSTANCE;
    }

    /**
     * 需要在初始化前调用, 将userId以及deviceId从v2版本迁移到v3版本中
     */
    public void upgrade(Application context) {
        TrackerContext.init(context);
        if (PersistentDataProvider.get().getString(KEEP_ID, null) == null) {
            upgradeDeviceId(context);
            upgradeUserId(context);
            PersistentDataProvider.get().putString(KEEP_ID, "true");
        }
    }

    @Override
    public IGrowingIO setUserAttributes(Map<String, ?> attributes) {
        HashMap<String, String> stringHashMap = new HashMap<>();
        for (String key : attributes.keySet()) {
            stringHashMap.put(key, String.valueOf(attributes.get(key)));
        }
        GrowingAutotracker.get().setLoginUserAttributes(stringHashMap);
        return this;
    }

    @Override
    public IGrowingIO setUserAttributes(JSONObject jsonObject) {
        GrowingAutotracker.get().setLoginUserAttributes(JsonUtil.copyToMap(jsonObject));
        return this;
    }

    @Override
    public IGrowingIO disableDataCollect() {
        GrowingAutotracker.get().setDataCollectionEnabled(false);
        return this;
    }

    @Override
    public IGrowingIO enableDataCollect() {
        GrowingAutotracker.get().setDataCollectionEnabled(true);
        return this;
    }

    @Override
    public String getDeviceId() {
        return GrowingAutotracker.get().getDeviceId();
    }

    @Override
    public String getVisitUserId() {
        return getDeviceId();
    }

    @Override
    public IGrowingIO setGeoLocation(double latitude, double longitude) {
        GrowingAutotracker.get().setLocation(latitude, longitude);
        return this;
    }

    @Override
    public IGrowingIO clearGeoLocation() {
        GrowingAutotracker.get().cleanLocation();
        return this;
    }

    @Override
    public IGrowingIO setUserId(String userId) {
        GrowingAutotracker.get().setLoginUserId(userId);
        return this;
    }

    @Override
    public IGrowingIO clearUserId() {
        GrowingAutotracker.get().cleanLoginUserId();
        return this;
    }

    @Override
    public IGrowingIO onNewIntent(Activity activity, Intent intent) {
        return this;
    }

    @Override
    public IGrowingIO track(String eventName) {
        GrowingAutotracker.get().trackCustomEvent(eventName);
        return this;
    }

    @Override
    public IGrowingIO track(String eventName, JSONObject var) {
        GrowingAutotracker.get().trackCustomEvent(eventName, JsonUtil.copyToMap(var));
        return this;
    }

    @Override
    public IGrowingIO track(String eventName, JSONObject var, String itemId, String itemKey) {
        GrowingAutotracker.get().trackCustomEvent(eventName, JsonUtil.copyToMap(var), itemKey, itemId);
        return this;
    }

    @Override
    public IGrowingIO trackPage(String pageName) {
        return trackPage(pageName, null);
    }

    @Override
    public IGrowingIO trackPage(String pageName, JSONObject var) {
        if (TextUtils.isEmpty(pageName)) {
            Log.e(TAG, "trackPage: pageName is NULL");
            return this;
        }

        if (!pageName.startsWith("/")) {
            pageName = "/" + pageName;
        }
        StringBuilder query = new StringBuilder();
        if (var != null) {
            for (Iterator<String> iterator = var.keys(); iterator.hasNext();) {
                String key = iterator.next();
                Object value = var.opt(key);
                query.append(key).append("=").append(value).append("&");
            }
            query.deleteCharAt(query.length() - 1);
        }

        TrackMainThread.trackMain().postEventToTrackMain(
                new HybridPageEvent.Builder()
                        .setPath(pageName)
                        .setTitle("")
                        .setQuery(query.toString())
                        .setTimestamp(System.currentTimeMillis()));
        return this;
    }

    @Override
    public IGrowingIO setImeiEnable(boolean imeiEnable) {
        return this;
    }

    @Override
    public IGrowingIO setAndroidIdEnable(boolean androidIdEnable) {
        return this;
    }

    @Override
    public IGrowingIO bridgeForWebView(Object webView) {
        return this;
    }

    @Override
    public IGrowingIO bridgeForX5WebView(Object x5WebView) {
        return this;
    }

    private void upgradeDeviceId(Context context) {
        String v2SpFileName = "growing_persist_data";
        SharedPreferences sharedPreferences = context.getSharedPreferences(v2SpFileName, Context.MODE_PRIVATE);
        String deviceId = sharedPreferences.getString("device_id", null);
        if (deviceId != null) {
            PersistentDataProvider.get().setDeviceId(deviceId);
        }
    }

    private void upgradeUserId(Context context) {
        String v2SpFileName = "growing_profile";
        SharedPreferences sharedPreferences = context.getSharedPreferences(v2SpFileName, Context.MODE_PRIVATE);
        String gioId = sharedPreferences.getString("pref_gio_id", null);
        if (gioId != null) {
            PersistentDataProvider.get().putString("GIO_ID", gioId);
        }

        File v2ShareFile = new File(context.getFilesDir(), ".gio.dir/gio.ipc.1");
        if (v2ShareFile.exists()) {
            FileChannel fileChannel = null;
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(v2ShareFile, "rw");
                fileChannel = randomAccessFile.getChannel();
                // 44 = 2(magic_num) + 2(process_num) + 40(process_id)
                // 44 = 4 * 11(modCount)
                // 4 = split   44 + 44 + 4 为variableBaseAddress变量存储区地址
                // 2 = sessionId len
                // 10 * 4 = sessionId data 44 + 44 + 4 + 2 + 10 为userId的start
                // 2 = userId len
                // 1000 * 4 = userId data
                ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 44 + 44 + 4 + 2 + 10 * 4, 44 + 44 + 4 + 2 + 10 + 2 + 1000 * 4);
                short len = byteBuffer.getShort();
                if (len != 0) {
                    byte[] result = new byte[len];
                    byteBuffer.get(result);
                    PersistentDataProvider.get().setLoginUserIdAndUserKey(new String(result), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
