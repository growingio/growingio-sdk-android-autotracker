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

package com.growingio.android.sdk.autotrack.webservices;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.View;

import com.growingio.android.sdk.autotrack.hybrid.HybridBridgeProvider;
import com.growingio.android.sdk.autotrack.hybrid.OnDomChangedListener;
import com.growingio.android.sdk.autotrack.view.DecorView;
import com.growingio.android.sdk.autotrack.view.OnViewStateChangedListener;
import com.growingio.android.sdk.autotrack.view.ViewStateChangedEvent;
import com.growingio.android.sdk.autotrack.view.ViewTreeStatusProvider;
import com.growingio.android.sdk.autotrack.view.WindowHelper;
import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.DeviceUtil;

import java.io.IOException;

public class ScreenshotProvider extends ListenerContainer<ScreenshotProvider.OnScreenshotRefreshedListener, String> {
    private static final String TAG = "ScreenshotProvider";

    private static final float SCREENSHOT_STANDARD_WIDTH = 720F;
    private static final long MIN_REFRESH_INTERVAL = 200L;

    private final float mScale;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private final Runnable mRefreshScreenshotRunnable = new Runnable() {
        @Override
        public void run() {
            dispatchScreenshot();
        }
    };

    private static class SingleInstance {
        private static final ScreenshotProvider INSTANCE = new ScreenshotProvider();
    }

    private ScreenshotProvider() {
        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(ContextProvider.getApplicationContext());
        mScale = SCREENSHOT_STANDARD_WIDTH / Math.min(metrics.widthPixels, metrics.heightPixels);

        mHandlerThread = new HandlerThread("ScreenshotProvider");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        ViewTreeStatusProvider.get().register(new OnViewStateChangedListener() {
            @Override
            public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
                Logger.d(TAG, "onViewStateChanged: " + changedEvent.getStateType());
                refreshScreenshot();
            }
        });
        HybridBridgeProvider.get().registerDomChangedListener(new OnDomChangedListener() {
            @Override
            public void onDomChanged() {
                Logger.d(TAG, "onDomChanged: ");
                refreshScreenshot();
            }
        });
    }

    private void dispatchScreenshot() {
        DecorView[] decorViews = WindowHelper.get().getTopActivityViews();
        if (decorViews.length == 0) {
            return;
        }
        View topView = decorViews[decorViews.length - 1].getView();
        topView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Logger.d(TAG, "Top view onViewDetachedFromWindow: ");
                refreshScreenshot();
            }
        });

        topView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(mScale);
                    dispatchActions(screenshotBase64);
                } catch (IOException e) {
                    Logger.e(TAG, e);
                }
            }
        });
    }

    private void refreshScreenshot() {
        mHandler.removeCallbacks(mRefreshScreenshotRunnable);
        mHandler.postDelayed(mRefreshScreenshotRunnable, MIN_REFRESH_INTERVAL);
    }

    public static ScreenshotProvider get() {
        return SingleInstance.INSTANCE;
    }

    @Override
    protected void singleAction(OnScreenshotRefreshedListener listener, String action) {
        listener.onScreenshotRefreshed(action, mScale);
    }

    public void registerScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        register(listener);
        refreshScreenshot();
    }

    public void unregisterScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        unregister(listener);
    }

    public interface OnScreenshotRefreshedListener {
        void onScreenshotRefreshed(String screenshotBase64, float scale);
    }
}