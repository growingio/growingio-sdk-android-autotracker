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

package com.growingio.android.sdk.track;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.interfaces.IActionCallback;
import com.growingio.android.sdk.track.interfaces.IEventSaveListener;
import com.growingio.android.sdk.track.middleware.EventSaver;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.EventSenderProvider;
import com.growingio.android.sdk.track.providers.LocationProvider;
import com.growingio.android.sdk.track.providers.ProjectInfoProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * GrowingIO主线程
 */
public class GIOMainThread implements IEventSaveListener {
    private static final String TAG = "GIO.Main";


    private static final int MSG_INIT_SDK = 1;
    private static final int MSG_SET_USER_ID = 2;
    private static final int MSG_SET_LOCATION = 3;
    private static final int MSG_POST_GEVENT = 4;
    private static final int MSG_POST_ACTION = 5;
    private static final long WAIT_TIME_OUT = 5L;
    private static CountDownLatch sCountDownLatch;
    private Handler mMainHandler;
    private Context mContext;

    private CoreAppState mCoreAppState;
    private EventSaver mEventSaver;
    private Looper mMainLooper;

    public GIOMainThread(Context context) {
        mContext = context;
        mCoreAppState = new CoreAppState(mContext, this);
        mEventSaver = new EventSaver(mContext, EventSenderProvider.EventSenderPolicy.get().getEventSender());
        initMainThread();
        ListenerContainer.eventSaveListeners().register(this);
    }

    public static void initWaitLock() {
        sCountDownLatch = new CountDownLatch(1);
    }

    public static void acquireWaitLock() {
        if (sCountDownLatch != null) {
            try {
                sCountDownLatch.await(WAIT_TIME_OUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void releaseWaitLock() {
        if (sCountDownLatch != null) {
            sCountDownLatch.countDown();
        }
    }

    public Context getContext() {
        return mContext;
    }

    public CoreAppState getCoreAppState() {
        return mCoreAppState;
    }

    private void initMainThread() {
        if (mMainHandler != null) {
            return;
        }
        synchronized (this) {
            if (mMainHandler != null) {
                return;
            }
            HandlerThread gioMainThread = new HandlerThread(TAG);
            gioMainThread.start();
            mMainLooper = gioMainThread.getLooper();
            mMainHandler = new H(mMainLooper);
            mMainHandler.sendEmptyMessage(MSG_INIT_SDK);
        }
    }

    public Looper getMainLooper() {
        initMainThread();
        return mMainLooper;
    }

    @GMainThread
    void initSDK() {
        checkAccountInfo();
        mCoreAppState.initInGIOMain();

        acquireWaitLock();
        ListenerContainer.gioMainInitSDKListeners().dispatchActions(null);
    }

    @GMainThread
    void checkAccountInfo() {
        ProjectInfoProvider policy = ProjectInfoProvider.AccountInfoPolicy.get();
        String projectId = policy.getProjectId();
        if (TextUtils.isEmpty(projectId)) {
            throw new IllegalStateException("未检测到有效的项目ID, 请参考帮助文档 https://docs.growingio.com/docs/developer-manual/sdkintegrated/android-sdk/auto-android-sdk");
        }
        if (TextUtils.isEmpty(policy.getUrlScheme())) {
            throw new IllegalStateException("未检测到有效的URL Scheme, 请参考帮助文档 https://docs.growingio.com/docs/developer-manual/sdkintegrated/android-sdk/auto-android-sdk");
        }
    }

    public void postEventToGMain(BaseEvent.BaseEventBuilder<?> eventBuilder) {
        if (eventBuilder == null) {
            return;
        }
        if (GConfig.getInstance().isEnableDataCollect()) {
            initMainThread();
            Message msg = mMainHandler.obtainMessage(MSG_POST_GEVENT);
            msg.obj = eventBuilder;
            mMainHandler.sendMessage(msg);
        }
    }

    public void postActionToGMain(IActionCallback callback) {
        if (callback == null) {
            return;
        }
        initMainThread();
        Message msg = mMainHandler.obtainMessage(MSG_POST_ACTION);
        msg.obj = callback;
        mMainHandler.sendMessage(msg);
    }

    public void setUserIdToGMain(String userId) {
        initMainThread();
        Message msg = mMainHandler.obtainMessage(MSG_SET_USER_ID);
        msg.obj = userId;
        mMainHandler.sendMessage(msg);
    }

    public void setLocationToGMain(Double latitude, Double longitude) {
        initMainThread();
        Message msg = mMainHandler.obtainMessage(MSG_SET_LOCATION);
        Pair<Double, Double> pair = new Pair<>(latitude, longitude);
        msg.obj = pair;
        mMainHandler.sendMessage(msg);
    }

    @GMainThread
    void onGenerateGEvent(BaseEvent.BaseEventBuilder<?> gEvent) {
        gEvent.readPropertyInGMain();
        ListenerContainer.eventSaveListeners().onEventSaved(gEvent.build());
    }

    @Override
    @GMainThread
    public void onEventSaved(GEvent gEvent) {
        if (gEvent instanceof BaseEvent) {
            LogUtil.printJson(TAG, "save: event, type is " + ((BaseEvent) gEvent).getEventType(), ((BaseEvent) gEvent).toJSONObject().toString());
        }
        mEventSaver.saveEvent(gEvent);
    }

    private class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_INIT_SDK:
                    initSDK();
                    break;
                case MSG_SET_USER_ID:
                    String userId = (String) msg.obj;
                    UserInfoProvider.UserInfoPolicy.get(mCoreAppState).setUserId(userId);
                    break;
                case MSG_SET_LOCATION:
                    @SuppressWarnings("unchecked")
                    Pair<Double, Double> location = (Pair<Double, Double>) msg.obj;
                    LocationProvider.LocationPolicy.get(mCoreAppState).setLocation(location.first, location.second);
                    break;
                case MSG_POST_GEVENT: {
                    BaseEvent.BaseEventBuilder<?> eventBuilder = (BaseEvent.BaseEventBuilder<?>) msg.obj;
                    onGenerateGEvent(eventBuilder);
                    break;
                }
                case MSG_POST_ACTION: {
                    IActionCallback callback = (IActionCallback) msg.obj;
                    callback.action();
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    }

}
