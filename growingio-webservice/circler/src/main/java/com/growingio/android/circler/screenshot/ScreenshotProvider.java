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

import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.View;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.data.HybridDom;
import com.growingio.android.sdk.track.modelloader.data.HybridJson;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.view.ScreenshotUtil;
import com.growingio.android.sdk.track.view.ViewTreeStatusProvider;
import com.growingio.android.sdk.track.view.WindowHelper;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import java.io.IOException;
import java.util.List;

public class ScreenshotProvider extends ListenerContainer<ScreenshotProvider.OnScreenshotRefreshedListener, CircleScreenshot> {
    private static final String TAG = "ScreenshotProvider";

    private static final float SCREENSHOT_STANDARD_WIDTH = 720F;
    private static final long MIN_REFRESH_INTERVAL = 500L;
    private long lastSendTime = System.currentTimeMillis();// 记录上次发送的事件，用来避免当界面刷新频率过快时一直无法发送圈选的情况。

    private final float mScale;
    private final Handler mHandler;
    private final Runnable mRefreshScreenshotRunnable = this::dispatchScreenshot;
    private ModelLoader<HybridDom, HybridJson> modelLoader;


    private static class SingleInstance {
        private static final ScreenshotProvider INSTANCE = new ScreenshotProvider();
    }

    private ScreenshotProvider() {
        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
        mScale = SCREENSHOT_STANDARD_WIDTH / Math.min(metrics.widthPixels, metrics.heightPixels);

        HandlerThread mHandlerThread = new HandlerThread("ScreenshotProvider");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        ViewTreeStatusProvider.get().register(changedEvent -> {
            if (System.currentTimeMillis() - lastSendTime >= MIN_REFRESH_INTERVAL * 2) {
                lastSendTime = System.currentTimeMillis();
                mHandler.post(this::dispatchScreenshot);
            } else {
                refreshScreenshot();
            }
        });

        getHybridModelLoader();
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
        if (getListenerCount() == 0) return;
        List<DecorView> decorViews = WindowHelper.get().getTopActivityViews();
        if (decorViews.isEmpty()) {
            return;
        }
        for (int i = decorViews.size() - 1; i >= 0; i--) {
            if (decorViews.get(i).getView() instanceof TipView) {
                decorViews.remove(i);
                break;
            }
        }
        View topView = decorViews.get(decorViews.size() - 1).getView();
        topView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                refreshScreenshot();
            }
        });

        topView.post(() -> {
            try {
                String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(mScale);
                sendScreenshotRefreshed(screenshotBase64, mScale);
            } catch (IOException e) {
                Logger.e(TAG, e);
            }
        });
    }

    public void refreshScreenshot() {
        mHandler.removeCallbacks(mRefreshScreenshotRunnable);
        mHandler.postDelayed(mRefreshScreenshotRunnable, MIN_REFRESH_INTERVAL);
    }

    public static ScreenshotProvider get() {
        return SingleInstance.INSTANCE;
    }

    @Override
    protected void singleAction(OnScreenshotRefreshedListener listener, CircleScreenshot action) {
        listener.onScreenshotRefreshed(action);
    }

    public void registerScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        register(listener);
        refreshScreenshot();
    }

    public void unregisterScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        unregister(listener);
    }

    public interface OnScreenshotRefreshedListener {
        void onScreenshotRefreshed(CircleScreenshot screenshot);
    }

    private long mSnapshotKey = 0;

    public void sendScreenshotRefreshed(String screenshotBase64, float scale) {
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
        if (result != null) dispatchActions(result);
    }

}