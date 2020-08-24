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

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.interfaces.OnTrackMainInitSDKCallback;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.ipc.GrowingIOIPC;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.OnUserIdChangedListener;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.util.UUID;

/**
 * 1. LoginUserId设置从空到非空，visit不发送，sessionId不变
 * 2. LoginUserId设置从非空到空，visit不发送，sessionId不变
 * 3. LoginUserId设置从"A"到"B", visit发送，sessionId改变
 * 4. LoginUserId设置从"A"到空再到"B", visit发送，sessionId改变
 * 5. LoginUserId设置从"A"到"A"，visit不发送，sessionId不变
 * 6. location任意精度从null到非null，重新发visit事件，sessionId不变
 * <p>
 * 总结出一句话，一个sessionId不能有两个用户ID
 */
public class SessionProvider implements IActivityLifecycle, OnUserIdChangedListener, OnTrackMainInitSDKCallback {
    private static final String TAG = "SessionProvider";
    private final GrowingIOIPC mIPC;

    private volatile boolean mAlreadySendVisit = false;
    private String mLatestNonNullUserId;
    private int mActivityStartCount = 0;
    private final long mSessionInterval;
    private double mLatitude = 0;
    private double mLongitude = 0;

    private static class SingleInstance {
        private static final SessionProvider INSTANCE = new SessionProvider();
    }

    private SessionProvider() {
        mSessionInterval = ConfigurationProvider.get().getTrackConfiguration().getSessionInterval();
        mIPC = PersistentDataProvider.get().getIPC();
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public static SessionProvider get() {
        return SingleInstance.INSTANCE;
    }

    @TrackThread
    void checkAndSendVisit(long resumeTime) {
        long lastPause = mIPC.getLastPauseTime();
        if (resumeTime - lastPause >= mSessionInterval) {
            String sessionId = refreshSessionId();
            generateVisit(sessionId, resumeTime);
        }
    }

    public String getSessionId() {
        return mIPC.getSessionId();
    }

    private String refreshSessionId() {
        String sessionId = UUID.randomUUID().toString();
        mIPC.setSessionId(sessionId);
        return sessionId;
    }

    @TrackThread
    public void resendVisit() {
        generateVisit(getSessionId(), mIPC.getLastResumeTime());
    }

    private void generateVisit(String sessionId, long timestamp) {
        mAlreadySendVisit = true;
        TrackEventGenerator.generateVisitEvent(sessionId, timestamp, mLatitude, mLongitude);
    }

    public boolean createdSession() {
        return mAlreadySendVisit;
    }

    public void forceReissueVisit() {
        if (mAlreadySendVisit) {
            return;
        }
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                String sessionId = refreshSessionId();
                long eventTime = System.currentTimeMillis();
                mIPC.setLastResumeTime(eventTime);
                generateVisit(sessionId, eventTime);
            }
        });
    }

    public void setLocation(double latitude, double longitude) {
        double eps = 1e-5;
        if (Math.abs(latitude) < eps && Math.abs(longitude) < eps) {
            LogUtil.d(TAG, "found invalid latitude and longitude, and return: ", latitude, ", ", longitude);
            return;
        }

        if ((mLatitude == 0 && Math.abs(latitude) > eps) ||
                (mLongitude == 0 && Math.abs(longitude) > eps)) {
            resendVisit();
        }

        mLatitude = latitude;
        mLongitude = longitude;
    }

    public void cleanLocation() {
        mLatitude = 0;
        mLongitude = 0;
    }

    @Override
    public void onActivityLifecycle(final ActivityLifecycleEvent event) {
        if (!ConfigurationProvider.get().getTrackConfiguration().isDataCollectionEnabled()) {
            return;
        }

        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
            if (mActivityStartCount == 0) {
                TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
                    @Override
                    public void run() {
                        long eventTime = System.currentTimeMillis();
                        checkAndSendVisit(eventTime);
                        mIPC.setLastResumeTime(eventTime);
                    }
                });
            }
            mActivityStartCount++;
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
            mActivityStartCount--;
            if (mActivityStartCount == 0) {
                TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
                    @Override
                    public void run() {
                        TrackEventGenerator.generateAppClosedEvent();
                        mIPC.setLastPauseTime(System.currentTimeMillis());
                    }
                });
            }
        }
    }

    @Override
    public void onTrackMainInitSDK() {
        mLatestNonNullUserId = UserInfoProvider.get().getUserId();
        UserInfoProvider.get().registerUserIdChangedListener(this);
    }


    @Override
    public void onUserIdChanged(@Nullable String newUserId) {
        LogUtil.d(TAG, "onUserIdChanged: newUserId = " + newUserId + ", mLatestNonNullUserId = " + mLatestNonNullUserId);

        if (!TextUtils.isEmpty(newUserId)
                && !TextUtils.isEmpty(mLatestNonNullUserId)
                && !newUserId.equals(mLatestNonNullUserId)) {
            String sessionId = refreshSessionId();
            long eventTime = System.currentTimeMillis();
            mIPC.setLastResumeTime(eventTime);
            generateVisit(sessionId, eventTime);
        }

        if (!TextUtils.isEmpty(newUserId)) {
            mLatestNonNullUserId = newUserId;
        }
    }
}