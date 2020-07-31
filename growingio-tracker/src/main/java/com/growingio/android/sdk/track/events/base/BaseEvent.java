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

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.ProjectInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseEvent extends GEvent {
    private final long mTimestamp;
    private final EventType mEventType;
    private final String mDeviceId;
    private final String mSessionId;
    private final String mDomain;
    private final String mUserId;
    private final boolean mIsInteractive;

    protected BaseEvent(BaseEventBuilder<?> eventBuilder) {
        mTimestamp = eventBuilder.mTimestamp;
        mEventType = eventBuilder.mEventType;
        mDeviceId = eventBuilder.mDeviceId;
        mSessionId = eventBuilder.mSessionId;
        mDomain = eventBuilder.mDomain;
        mUserId = eventBuilder.mUserId;
        mIsInteractive = eventBuilder.mIsInteractive;
    }

    @Override
    public String getTag() {
        return getEventType().toString();
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public EventType getEventType() {
        return mEventType;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getUserId() {
        return mUserId;
    }

    public boolean isInteractive() {
        return mIsInteractive;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("mTimestamp", mTimestamp);
            json.put("mEventType", mEventType);
            json.put("mDeviceId", mDeviceId);
            json.put("mSessionId", mSessionId);
            json.put("mDomain", mDomain);
            json.put("mUserId", mUserId);
            json.put("mIsInteractive", mIsInteractive);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static abstract class BaseEventBuilder<T extends BaseEvent> {
        protected final CoreAppState mCoreAppState;

        protected EventType mEventType;
        protected long mTimestamp;
        protected String mSessionId;
        protected String mDomain;
        private String mDeviceId;
        private String mUserId;
        private boolean mIsInteractive;

        protected BaseEventBuilder(CoreAppState coreAppState) {
            mCoreAppState = coreAppState;
            mTimestamp = System.currentTimeMillis();
            mEventType = getEventType();
            mIsInteractive = ActivityStateProvider.ActivityStatePolicy.get().getResumedActivity() != null;
            mDomain = ProjectInfoProvider.AccountInfoPolicy.get().getPackageName(mCoreAppState.getGlobalContext());
        }

        @GMainThread
        @CallSuper
        public void readPropertyInGMain() {
            mDeviceId = DeviceInfoProvider.DeviceInfoPolicy.get(mCoreAppState.getGlobalContext()).getDeviceId();
            mSessionId = mCoreAppState.getGrowingIOIPC().getSessionId();
            mUserId = mCoreAppState.getGrowingIOIPC().getUserId();
        }

        public CoreAppState getCoreAppState() {
            return mCoreAppState;
        }

        public long getTimestamp() {
            return mTimestamp;
        }

        public String getSessionId() {
            return mSessionId;
        }

        public String getDeviceId() {
            return mDeviceId;
        }

        public String getDomain() {
            return mDomain;
        }

        public String getUserId() {
            return mUserId;
        }

        public boolean isInteractive() {
            return mIsInteractive;
        }

        public abstract EventType getEventType();

        public abstract T build();
    }

}
