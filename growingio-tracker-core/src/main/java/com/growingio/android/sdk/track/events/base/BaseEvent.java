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

package com.growingio.android.sdk.track.events.base;


import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.growingio.android.sdk.track.data.EventSequenceId;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseEvent extends GEvent {
    private static final String APP_STATE_FOREGROUND = "FOREGROUND";
    private static final String APP_STATE_BACKGROUND = "BACKGROUND";

    private final String mDeviceId;
    private final String mUserId;
    private final String mSessionId;
    private final String mEventType;
    private final long mTimestamp;
    private final String mDomain;
    private final String mUrlScheme;
    private final String mAppState;
    private final long mGlobalSequenceId;
    private final long mEventSequenceId;
    private final Map<String, String> mExtraParams;

    protected BaseEvent(BaseBuilder<?> eventBuilder) {
        mDeviceId = eventBuilder.mDeviceId;
        mUserId = eventBuilder.mUserId;
        mSessionId = eventBuilder.mSessionId;
        mEventType = eventBuilder.mEventType;
        mTimestamp = eventBuilder.mTimestamp;
        mDomain = eventBuilder.mDomain;
        mUrlScheme = eventBuilder.mUrlScheme;
        mAppState = eventBuilder.mAppState;
        mGlobalSequenceId = eventBuilder.mGlobalSequenceId;
        mEventSequenceId = eventBuilder.mEventSequenceId;
        mExtraParams = eventBuilder.mExtraParams;
    }

    public static String getAppStateForeground() {
        return APP_STATE_FOREGROUND;
    }

    public static String getAppStateBackground() {
        return APP_STATE_BACKGROUND;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getUrlScheme() {
        return mUrlScheme;
    }

    public String getAppState() {
        return mAppState;
    }

    public long getGlobalSequenceId() {
        return mGlobalSequenceId;
    }

    public long getEventSequenceId() {
        return mEventSequenceId;
    }

    @Override
    public String getEventType() {
        return mEventType;
    }

    public JSONObject toJSONObject() {
        JSONObject json;
        if (!mExtraParams.isEmpty()) {
            json = new JSONObject(mExtraParams);
        } else {
            json = new JSONObject();
        }
        try {
            json.put("deviceId", mDeviceId);
            if (!TextUtils.isEmpty(mUserId)) {
                json.put("userId", mUserId);
            }
            json.put("sessionId", mSessionId);
            json.put("eventType", mEventType);
            json.put("timestamp", mTimestamp);
            json.put("domain", mDomain);
            json.put("urlScheme", mUrlScheme);
            json.put("appState", mAppState);
            json.put("globalSequenceId", mGlobalSequenceId);
            json.put("eventSequenceId", mEventSequenceId);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static abstract class BaseBuilder<T extends BaseEvent> {
        private String mDeviceId;
        private String mUserId;
        protected String mSessionId;
        protected String mEventType;
        protected long mTimestamp;
        protected String mDomain;
        private final String mUrlScheme;
        private final String mAppState;
        private long mGlobalSequenceId;
        private long mEventSequenceId;
        private final Map<String, String> mExtraParams = new HashMap<>();

        protected BaseBuilder() {
            mTimestamp = System.currentTimeMillis();
            mEventType = getEventType();
            mAppState = ActivityStateProvider.get().getForegroundActivity() != null ? APP_STATE_FOREGROUND : APP_STATE_BACKGROUND;
            mDomain = ConfigurationProvider.get().getPackageName();
            mUrlScheme = ConfigurationProvider.get().getTrackConfiguration().getUrlScheme();
        }

        @TrackThread
        @CallSuper
        public void readPropertyInTrackThread() {
            mDeviceId = DeviceInfoProvider.get().getDeviceId();
            mSessionId = SessionProvider.get().getSessionId();
            mUserId = UserInfoProvider.get().getLoginUserId();
            EventSequenceId sequenceId = PersistentDataProvider.get().getAndIncrement(mEventType);
            mGlobalSequenceId = sequenceId.getGlobalId();
            mEventSequenceId = sequenceId.getEventTypeId();
        }

        public BaseBuilder<?> addExtraParam(String key, String value) {
            mExtraParams.put(key, value);
            return this;
        }

        public abstract String getEventType();

        public abstract T build();
    }

}
