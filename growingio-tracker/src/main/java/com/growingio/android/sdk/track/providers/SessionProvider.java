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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.interfaces.IActionCallback;
import com.growingio.android.sdk.track.interfaces.OnGIOMainInitSDK;
import com.growingio.android.sdk.track.ipc.GrowingIOIPC;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.OnUserIdChangedListener;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.util.UUID;

/**
 * 1. userid设置从空到非空，visit不发送，sessionid不变
 * 2. userid设置从非空到空，visit不发送，sessionid不变
 * 3. userid设置从"A"到"B", visit发送，sessionid改变
 * 4. userid设置从"A"到空再到"B", visit发送，sessionid改变
 * 5. userid设置从"A"到"A"，visit不发送，sessionID不变
 */
public interface SessionProvider extends OnGIOMainInitSDK {

    void resendVisit();

    void forceReissueVisit();

    void setSessionInterval(long sessionInterval);

    long sessionInterval();

    class SessionPolicy implements SessionProvider, IActivityLifecycle, OnUserIdChangedListener {
        private static final String TAG = "GIO.SessionPolicy";
        private static SessionProvider sInstance;

        private GrowingIOIPC mGrowingIOIPC;
        private CoreAppState mCoreAppState;

        private volatile boolean mAlreadySendVisit = false;
        private String mLatestNonNullUserId;
        private int mActivityStartCount = 0;
        private long mSessionInterval = 30_000;

        public SessionPolicy(@NonNull CoreAppState coreAppState) {
            mCoreAppState = coreAppState;
            mGrowingIOIPC = coreAppState.getGrowingIOIPC();
        }

        public static SessionProvider get(final CoreAppState coreAppState) {
            if (sInstance == null) {
                synchronized (SessionPolicy.class) {
                    if (sInstance == null) {
                        sInstance = GIOProviders.provider(SessionProvider.class, new GIOProviders.DefaultCallback<SessionProvider>() {
                            @Override
                            public SessionProvider value() {
                                return new SessionPolicy(coreAppState);
                            }
                        });
                    }
                }
            }
            return sInstance;
        }

        @GMainThread
        void checkAndSendVisit(long resumeTime) {
            long lastPause = mGrowingIOIPC.getLastPauseTime();
            if (resumeTime - lastPause >= mSessionInterval) {
                String sessionId = refreshSessionId();
                generateVisit(sessionId, resumeTime);
            }
        }

        private String getSessionId() {
            return mGrowingIOIPC.getSessionId();
        }

        private String refreshSessionId() {
            String sessionId = UUID.randomUUID().toString();
            mGrowingIOIPC.setSessionId(sessionId);
            return sessionId;
        }

        @GMainThread
        @Override
        public void resendVisit() {
            generateVisit(getSessionId(), mGrowingIOIPC.getLastResumeTime());
        }

        private void generateVisit(String sessionId, long timestamp) {
            mAlreadySendVisit = true;
            EventCoreGeneratorProvider.EventCoreGenerator.get(mCoreAppState).generateVisit(sessionId, timestamp);
        }

        @Override
        public void forceReissueVisit() {
            if (mAlreadySendVisit) {
                return;
            }
            mCoreAppState.getGIOMain().postActionToGMain(new IActionCallback() {
                @Override
                public void action() {
                    String sessionId = refreshSessionId();
                    long eventTime = System.currentTimeMillis();
                    mGrowingIOIPC.setLastResumeTime(eventTime);
                    generateVisit(sessionId, eventTime);
                }
            });
        }

        @Override
        public void setSessionInterval(long sessionInterval) {
            this.mSessionInterval = sessionInterval;
        }

        @Override
        public void onActivityLifecycle(final ActivityLifecycleEvent event) {
            if (!GConfig.getInstance().isEnableDataCollect()) {
                return;
            }

            if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
                if (mActivityStartCount == 0) {
                    mCoreAppState.getGIOMain().postActionToGMain(new IActionCallback() {
                        @Override
                        public void action() {
                            long eventTime = System.currentTimeMillis();
                            checkAndSendVisit(eventTime);
                            mGrowingIOIPC.setLastResumeTime(eventTime);
                        }
                    });
                }
                mActivityStartCount++;
            } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
                mActivityStartCount--;
                if (mActivityStartCount == 0) {
                    mCoreAppState.getGIOMain().postActionToGMain(new IActionCallback() {
                        @Override
                        public void action() {
                            mGrowingIOIPC.setLastPauseTime(System.currentTimeMillis());
                        }
                    });
                }
            }
        }

        @Override
        public long sessionInterval() {
            return mSessionInterval;
        }

        @Override
        public void onGIOMainInitSDK() {
            ActivityStateProvider.ActivityStatePolicy.get().registerActivityLifecycleListener(this);
            mLatestNonNullUserId = UserInfoProvider.UserInfoPolicy.get(mCoreAppState).getUserId();
            UserInfoProvider.UserInfoPolicy.get(mCoreAppState).registerUserIdChangedListener(this);
        }


        @Override
        public void onUserIdChanged(@Nullable String newUserId) {
            LogUtil.d(TAG, "onUserIdChanged: newUserId = " + newUserId + ", mLatestNonNullUserId = " + mLatestNonNullUserId);

            if (!TextUtils.isEmpty(newUserId)
                    && !TextUtils.isEmpty(mLatestNonNullUserId)
                    && !newUserId.equals(mLatestNonNullUserId)) {
                String sessionId = refreshSessionId();
                long eventTime = System.currentTimeMillis();
                mGrowingIOIPC.setLastResumeTime(eventTime);
                generateVisit(sessionId, eventTime);
            }

            if (!TextUtils.isEmpty(newUserId)) {
                mLatestNonNullUserId = newUserId;
            }
        }
    }
}
