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

package com.growingio.android.circler.screenshot;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.view.ScreenElementHelper;
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
import com.growingio.android.sdk.track.webservices.Circler;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class ScreenshotProvider extends ViewTreeStatusListener {
    private static final String TAG = "ScreenshotProvider";

    private static final float SCREENSHOT_STANDARD_WIDTH = 720F;
    private static final long MIN_REFRESH_INTERVAL = 500L;
    private static final long EVENT_REFRESH_INTERVAL = 1000L;
    private static final long MAX_REFRESH_INTERVAL = 3000L;
    private long lastSendTime = System.currentTimeMillis(); // 记录上次发送的事件，用来避免当界面刷新频率过快时一直无法发送圈选的情况。

    private final float mScale;
    private final Handler mHandler;
    private final Runnable mRefreshScreenshotRunnable = this::dispatchScreenshot;
    private ModelLoader<HybridDom, HybridJson> modelLoader;

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

    ModelLoader<HybridDom, HybridJson> getHybridModelLoader() {
        if (modelLoader == null) {
            modelLoader = TrackerContext.get().getRegistry().getModelLoader(HybridDom.class, HybridJson.class);
            if (modelLoader != null) {
                modelLoader.buildLoadData(new HybridDom(this::refreshScreenshot)).fetcher.executeData();
            }
        }
        return modelLoader;
    }

    private void dispatchScreenshot() {
        if (mListener == null) return;
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) return;

        try {
            ScreenshotUtil.getScreenshotBitmap(mScale, bitmap -> {
                try {
                    String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(bitmap);
                    sendScreenshotRefreshed(screenshotBase64, mScale);
                } catch (IOException e) {
                    Logger.e(TAG, "base64 screenshot failed:" + e.getMessage());
                }
            });
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, "dispatch screenshot failed:" + e.getMessage());
        }
    }

    private void refreshScreenshot(long duration) {
        mHandler.removeCallbacks(mRefreshScreenshotRunnable);
        mHandler.postDelayed(mRefreshScreenshotRunnable, duration);
    }

    public void refreshScreenshot() {
        refreshScreenshot(MIN_REFRESH_INTERVAL);
    }

    public static ScreenshotProvider get() {
        return SingleInstance.INSTANCE;
    }

    public void registerScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        register();
        this.mListener = listener;
        refreshScreenshot();
    }

    public void unregisterScreenshotRefreshedListener() {
        this.mListener = null;
        unRegister();
    }

    public interface OnScreenshotRefreshedListener {
        void onScreenshotRefreshed(CircleScreenshot screenshot);
    }

    private long mSnapshotKey = 0;

    private void sendScreenshotRefreshed(String screenshotBase64, float scale) {
        lastSendTime = System.currentTimeMillis();
        CircleScreenshot.Builder builder = new CircleScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++);

        builder.build(new Callback<CircleScreenshot>() {
            @Override
            public void onSuccess(CircleScreenshot result) {
                Logger.d(TAG, "Create circle screenshot successfully");
                sendScreenshot(result);
            }

            @Override
            public void onFailed() {
                Logger.e(TAG, "Create circle screenshot failed");
            }
        });
    }

    public void sendScreenshot(CircleScreenshot result) {
        if (result != null && mListener != null) {
            this.mListener.onScreenshotRefreshed(result);
        }
    }

    public void generateCircleData(Circler.CirclerData data) {
        if (mListener == null) return;
        mHandler.removeMessages(0);
        Message message = Message.obtain(mHandler, new CircleDataThread(data));
        message.what = 0;
        mHandler.sendMessageDelayed(message, 100L);
    }

    private class CircleDataThread implements Runnable {
        final Circler.CirclerData circlerData;

        CircleDataThread(Circler.CirclerData circlerData) {
            this.circlerData = circlerData;
        }

        @Override
        public void run() {
            lastSendTime = System.currentTimeMillis();


            CircleScreenshot.Builder builder = new CircleScreenshot.Builder()
                    .setScale((float) circlerData.getScale())
                    .setScreenWidth((int) circlerData.getWidth())
                    .setScreenHeight((int) circlerData.getHeight())
                    .setSnapshotKey(mSnapshotKey++);

            for (int i = 0; i < circlerData.getElements().size(); i++) {
                Map<String, Object> data = circlerData.getElements().get(i);
                JSONObject viewJson = ScreenElementHelper.createViewElementWithMap(data);
                if (viewJson != null) {
                    builder.addElement(viewJson);
                }
            }

            for (int i = 0; i < circlerData.getPages().size(); i++) {
                Map<String, Object> data = circlerData.getPages().get(i);
                JSONObject pageJson = ScreenElementHelper.createPageElementWithMap(data);
                if (pageJson != null) {
                    builder.addPage(pageJson);
                }
            }


            if (circlerData.getScreenshot() == null) {
                try {
                    ScreenshotUtil.getScreenshotBitmap(mScale, bitmap -> {
                        try {
                            String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(bitmap);
                            builder.setScreenshot(screenshotBase64);
                            sendScreenshotWithBuilder(builder);
                        } catch (IOException e) {
                            Logger.e(TAG, "base64 screenshot failed:" + e.getMessage());
                        }
                    });
                } catch (IllegalArgumentException e) {
                    Logger.e(TAG, "dispatch screenshot failed:" + e.getMessage());
                }
            } else {
                builder.setScreenshot(circlerData.getScreenshot());
                sendScreenshotWithBuilder(builder);
            }
        }
    }

    private void sendScreenshotWithBuilder(CircleScreenshot.Builder builder) {
        builder.build(new Callback<CircleScreenshot>() {
            @Override
            public void onSuccess(CircleScreenshot result) {
                Logger.d(TAG, "Create circle screenshot successfully");
                sendScreenshot(result);
            }

            @Override
            public void onFailed() {
                Logger.e(TAG, "Create circle screenshot failed");
            }
        });
    }

}