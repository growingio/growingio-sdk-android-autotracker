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

import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.View;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.view.ScreenshotUtil;
import com.growingio.android.sdk.track.view.ViewTreeStatusProvider;
import com.growingio.android.sdk.track.view.WindowHelper;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import java.io.IOException;
import java.util.List;

public class ScreenshotProvider extends ListenerContainer<ScreenshotProvider.OnScreenshotRefreshedListener, DebuggerScreenshot> {
    private static final String TAG = "ScreenshotProvider";

    private static final float SCREENSHOT_STANDARD_WIDTH = 720F;
    private static final long MIN_REFRESH_INTERVAL = 300L;

    private final float mScale;
    private final Handler mHandler;
    private final Runnable mRefreshScreenshotRunnable = this::dispatchScreenshot;

    private static class SingleInstance {
        private static final ScreenshotProvider INSTANCE = new ScreenshotProvider();
    }

    private ScreenshotProvider() {
        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
        mScale = SCREENSHOT_STANDARD_WIDTH / Math.min(metrics.widthPixels, metrics.heightPixels);

        HandlerThread mHandlerThread = new HandlerThread("ScreenshotProvider");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        ViewTreeStatusProvider.get().register(changedEvent -> refreshScreenshot());

        ClassUtils.registerHybridScreenShot(this);
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

        topView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(mScale);
                    sendScreenshotRefreshed(screenshotBase64, mScale);
                } catch (IOException e) {
                    Logger.e(TAG, e);
                }
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
    protected void singleAction(OnScreenshotRefreshedListener listener, DebuggerScreenshot action) {
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
        void onScreenshotRefreshed(DebuggerScreenshot screenshot);
    }

    private long mSnapshotKey = 0;
    private Disposable mCircleScreenshotDisposable;

    public void sendScreenshotRefreshed(String screenshotBase64, float scale) {
        if (mCircleScreenshotDisposable != null) {
            mCircleScreenshotDisposable.dispose();
        }

        mCircleScreenshotDisposable = new DebuggerScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++)
                .build(new Callback<DebuggerScreenshot>() {
                    @Override
                    public void onSuccess(DebuggerScreenshot result) {
                        Logger.d(TAG, "Create circle screenshot successfully");
                        if (result != null) dispatchActions(result);
                    }

                    @Override
                    public void onFailed() {
                        Logger.e(TAG, "Create circle screenshot failed");
                    }
                });
    }
}