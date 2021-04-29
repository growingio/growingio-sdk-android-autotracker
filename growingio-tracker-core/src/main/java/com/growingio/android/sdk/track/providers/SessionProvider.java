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

package com.growingio.android.sdk.track.providers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.interfaces.OnTrackMainInitSDKCallback;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.OnUserIdChangedListener;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.SystemUtil;

import java.util.UUID;

public class SessionProvider implements IActivityLifecycle, OnUserIdChangedListener, OnTrackMainInitSDKCallback {
    private static final String TAG = "SessionProvider";

    private volatile boolean mAlreadySendVisit = false;
    private String mLatestNonNullUserId;
    private int mActivityStartCount = 0;
    private final long mSessionInterval;
    private double mLatitude = 0;
    private double mLongitude = 0;

    private long mLatestPauseTime = 0;
    private long mLatestVisitTime = 0;
    private volatile String mSessionId;
    private final Context mContext;

    private static class SingleInstance {
        private static final SessionProvider INSTANCE = new SessionProvider();
    }

    private SessionProvider() {
        mContext = ContextProvider.getApplicationContext();
        mSessionInterval = ConfigurationProvider.get().getTrackConfiguration().getSessionInterval() * 1000;
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public static SessionProvider get() {
        return SingleInstance.INSTANCE;
    }

    void checkAndSendVisit(long resumeTime) {
        if (mAlreadySendVisit && mLatestPauseTime == 0) {
            Logger.w(TAG, "Visit event already send by force reissue");
            return;
        }

        if (resumeTime - mLatestPauseTime >= mSessionInterval) {
            String sessionId = refreshSessionId();
            generateVisit(sessionId, resumeTime);
        }
    }

    public String getSessionId() {
        if (mSessionId != null) {
            return mSessionId;
        } else {
            return PersistentDataProvider.get().getSessionId();
        }
    }

    private String refreshSessionId() {
        mSessionId = UUID.randomUUID().toString();
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                PersistentDataProvider.get().setSessionId(mSessionId);
            }
        });
        return mSessionId;
    }

    @TrackThread
    public void resendVisit() {
        generateVisit(getSessionId(), mLatestVisitTime);
    }

    private void generateVisit(String sessionId, long timestamp) {
        mAlreadySendVisit = true;
        mLatestVisitTime = timestamp;
        TrackEventGenerator.generateVisitEvent(sessionId, timestamp);
    }

    public boolean createdSession() {
        return mAlreadySendVisit;
    }

    public void forceReissueVisit() {
        if (mAlreadySendVisit || !SystemUtil.isMainProcess(mContext)) {
            return;
        }
        String sessionId = refreshSessionId();
        long eventTime = System.currentTimeMillis();
        generateVisit(sessionId, eventTime);
    }

    public void setLocation(double latitude, double longitude) {
        double eps = 1e-5;
        if (Math.abs(latitude) < eps && Math.abs(longitude) < eps) {
            Logger.d(TAG, "found invalid latitude and longitude, and return: ", latitude, ", ", longitude);
            return;
        }

        // 只有在第一次设置位置信息的时候才会补发Visit，重设位置信息只会更新，不会重发visit
        if ((mLatitude == 0 && Math.abs(latitude) > eps) ||
                (mLongitude == 0 && Math.abs(longitude) > eps)) {
            mLatitude = latitude;
            mLongitude = longitude;
            resendVisit();
        } else {
            mLatitude = latitude;
            mLongitude = longitude;
        }
    }

    public void cleanLocation() {
        mLatitude = 0;
        mLongitude = 0;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public void onActivityLifecycle(final ActivityLifecycleEvent event) {
        if (!ConfigurationProvider.get().getTrackConfiguration().isDataCollectionEnabled()) {
            return;
        }

        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
            if (mActivityStartCount == 0) {
                checkAndSendVisit(System.currentTimeMillis());
            }
            mActivityStartCount++;
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
            mActivityStartCount--;
            if (mActivityStartCount == 0) {
                TrackEventGenerator.generateAppClosedEvent();
                mLatestPauseTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onTrackMainInitSDK() {
        mLatestNonNullUserId = UserInfoProvider.get().getLoginUserId();
        UserInfoProvider.get().registerUserIdChangedListener(this);
    }

    @Override
    public void onUserIdChanged(@Nullable String newUserId) {
        Logger.d(TAG, "onUserIdChanged: newUserId = " + newUserId + ", mLatestNonNullUserId = " + mLatestNonNullUserId);
        if (!TextUtils.isEmpty(newUserId)) {
            if (TextUtils.isEmpty(mLatestNonNullUserId)) {
                resendVisit();
            } else {
                if (!newUserId.equals(mLatestNonNullUserId)) {
                    String sessionId = refreshSessionId();
                    long eventTime = System.currentTimeMillis();
                    generateVisit(sessionId, eventTime);
                }
            }
            mLatestNonNullUserId = newUserId;
        }
    }
}