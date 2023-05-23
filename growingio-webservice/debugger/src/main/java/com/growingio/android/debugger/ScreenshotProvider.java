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

package com.growingio.android.debugger;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.hybrid.HybridDom;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.view.ScreenshotUtil;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.view.ViewTreeStatusListener;

import java.io.IOException;

public class ScreenshotProvider extends ViewTreeStatusListener {
    private static final String TAG = "ScreenshotProvider";

    private static final float SCREENSHOT_STANDARD_WIDTH = 720F;
    private static final long MIN_REFRESH_INTERVAL = 500L;
    private static final long EVENT_REFRESH_INTERVAL = 1000L;
    private static final long MAX_REFRESH_INTERVAL = 3000L;
    private long lastSendTime = 0L; // 记录上次发送的事件，用来避免当界面刷新频率过快时一直无法发送圈选事件。

    private final float mScale;
    private final Handler mHandler;
    private final Runnable mRefreshScreenshotRunnable = this::dispatchScreenshot;

    private OnScreenshotRefreshedListener mListener;


    private static class SingleInstance {
        private static final ScreenshotProvider INSTANCE = new ScreenshotProvider();
    }

    private ScreenshotProvider() {
        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
        mScale = SCREENSHOT_STANDARD_WIDTH / Math.min(metrics.widthPixels, metrics.heightPixels);

        HandlerThread mHandlerThread = new HandlerThread("ScreenshotProvider");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        getHybridModelLoader();
    }

    @Override
    public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
        if (System.currentTimeMillis() - lastSendTime >= MAX_REFRESH_INTERVAL) {
            lastSendTime = System.currentTimeMillis();
            mHandler.post(this::dispatchScreenshot);
        } else {
            if (changedEvent.getStateType() == ViewStateChangedEvent.StateType.MANUAL_CHANGED) {
                refreshScreenshot(EVENT_REFRESH_INTERVAL);
            } else {
                refreshScreenshot();
            }
        }
    }

    private void getHybridModelLoader() {
        ModelLoader<HybridDom, HybridJson> modelLoader = TrackerContext.get().getRegistry().getModelLoader(HybridDom.class, HybridJson.class);
        if (modelLoader != null) {
            modelLoader.buildLoadData(new HybridDom(this::refreshScreenshot)).fetcher.executeData();
        }
    }

    private void dispatchScreenshot() {
        if (mListener == null) return;
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) return;

        View topView = activity.getWindow().getDecorView();

        try {
            ScreenshotUtil.getScreenshotBitmap(mScale, bitmap -> topView.post(() -> {
                try {
                    String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(bitmap);
                    sendScreenshotRefreshed(screenshotBase64, mScale);
                } catch (IOException e) {
                    Logger.e(TAG, "base64 screenshot failed:" + e.getMessage());
                }
            }));
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, "dispatch screenshot failed:" + e.getMessage());
        }
    }

    private void refreshScreenshot(long duration) {
        mHandler.removeCallbacks(mRefreshScreenshotRunnable);
        mHandler.postDelayed(mRefreshScreenshotRunnable, duration);
    }

    private void refreshScreenshot() {
        refreshScreenshot(MIN_REFRESH_INTERVAL);
    }

    public void generateDebuggerData(String screenshotBase64) {
        if (mListener == null) return;
        mHandler.removeMessages(0);
        Message message = Message.obtain(mHandler, new Runnable() {
            @Override
            public void run() {
                sendScreenshotRefreshed(screenshotBase64, mScale);
            }
        });
        message.what = 0;
        mHandler.sendMessageDelayed(message, MIN_REFRESH_INTERVAL);
    }

    public static ScreenshotProvider get() {
        return SingleInstance.INSTANCE;
    }

    public void registerScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        register();
        mListener = listener;
        refreshScreenshot();
    }

    public void unregisterScreenshotRefreshedListener() {
        mListener = null;
        unRegister();
    }

    public interface OnScreenshotRefreshedListener {
        void onScreenshotRefreshed(DebuggerScreenshot screenshot);
    }

    private long mSnapshotKey = 0;

    public void sendScreenshotRefreshed(String screenshotBase64, float scale) {

        lastSendTime = System.currentTimeMillis();

        DebuggerScreenshot.Builder builder = new DebuggerScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++);
        builder.build(new Callback<DebuggerScreenshot>() {
            @Override
            public void onSuccess(DebuggerScreenshot result) {
                Logger.d(TAG, "Create circle screenshot successfully");
                if (result != null && mListener != null) mListener.onScreenshotRefreshed(result);
            }

            @Override
            public void onFailed() {
                Logger.e(TAG, "Create circle screenshot failed");
            }
        });
    }
}