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

package com.growingio.android.sdk.track.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.growingio.android.sdk.track.TrackerContext;
import com.growingio.android.sdk.track.ipc.IDataSharer;
import com.growingio.android.sdk.track.ipc.MultiProcessDataSharer;

public class PersistentDataProvider {
    private static final String SHARER_NAME = "PersistentSharerDataProvider";
    private static final int SHARER_MAX_SIZE = 50;

    private static final String KEY_TYPE_GLOBAL = "TYPE_GLOBAL";
    private static final String KEY_LOGIN_USER_ID = "LOGIN_USER_ID";
    private static final String KEY_DEVICE_ID = "DEVICE_ID";
    private static final String KEY_SESSION_ID = "SESSION_ID";

    private final IDataSharer mDataSharer;

    private static class SingleInstance {
        private static final PersistentDataProvider INSTANCE = new PersistentDataProvider();
    }

    private PersistentDataProvider() {
        Context context = TrackerContext.get().getApplicationContext();
        mDataSharer = new MultiProcessDataSharer(context, SHARER_NAME, SHARER_MAX_SIZE);
    }

    public static PersistentDataProvider get() {
        return SingleInstance.INSTANCE;
    }

    public EventSequenceId getAndIncrement(String eventType) {
        long globalId = mDataSharer.getAndIncrement(KEY_TYPE_GLOBAL, 1);
        long eventTypeId = mDataSharer.getAndIncrement(eventType, 1);
        return new EventSequenceId(globalId, eventTypeId);
    }

    public String getSessionId() {
        return mDataSharer.getString(KEY_SESSION_ID, "");
    }

    public void setSessionId(String sessionId) {
        mDataSharer.putString(KEY_SESSION_ID, sessionId);
    }

    public String getDeviceId() {
        return mDataSharer.getString(KEY_DEVICE_ID, "");
    }

    public void setDeviceId(@NonNull String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return;
        }
        mDataSharer.putString(KEY_DEVICE_ID, deviceId);
    }

    public String getLoginUserId() {
        return mDataSharer.getString(KEY_LOGIN_USER_ID, "");
    }

    public void setLoginUserId(@Nullable String userId) {
        mDataSharer.putString(KEY_LOGIN_USER_ID, userId);
    }

    public void putString(String key, @Nullable String value) {
        mDataSharer.putString(key, value);
    }

    @Nullable
    public String getString(String key, String defValue) {
        return mDataSharer.getString(key, defValue);
    }
}