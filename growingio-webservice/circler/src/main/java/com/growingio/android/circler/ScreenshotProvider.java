/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.circler;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.view.ScreenElementHelper;
import com.growingio.android.sdk.autotrack.view.ViewNodeProvider;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.listener.Callback;
import com.growingio.android.sdk.track.middleware.EventFlutter;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.hybrid.HybridDom;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.view.ScreenshotUtil;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.view.ViewTreeStatusListener;
import com.growingio.android.sdk.track.middleware.webservice.Circler;

import org.json.JSONException;
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

    private float scale;
    private final Handler screenshotHandler;
    private final Runnable refreshScreenshotRunnable = this::dispatchScreenshot;
    private OnScreenshotRefreshedListener refreshListener;

    private ThreadSafeTipView safeTipView;

    private ConfigurationProvider configurationProvider;
    private AppInfoProvider appInfoProvider;
    private DeviceInfoProvider deviceInfoProvider;
    private ViewNodeProvider viewNodeProvider;
    private TrackerRegistry registry;

    ScreenshotProvider() {
        HandlerThread mHandlerThread = new HandlerThread("ScreenshotProvider");
        mHandlerThread.start();
        screenshotHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void setup(TrackerContext context) {
        super.setup(context);
        configurationProvider = context.getConfigurationProvider();
        appInfoProvider = context.getProvider(AppInfoProvider.class);
        viewNodeProvider = context.getProvider(ViewNodeProvider.class);
        deviceInfoProvider = context.getDeviceInfoProvider();
        registry = context.getRegistry();
        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(context.getBaseContext());
        scale = SCREENSHOT_STANDARD_WIDTH / Math.min(metrics.widthPixels, metrics.heightPixels);

        safeTipView = new ThreadSafeTipView(context.getBaseContext(), activityStateProvider, appInfoProvider.getAppVersion());

        ModelLoader<HybridDom, HybridJson> modelLoader = context.getRegistry().getModelLoader(HybridDom.class, HybridJson.class);
        if (modelLoader != null) {
            modelLoader.buildLoadData(new HybridDom(this::refreshScreenshot)).fetcher.executeData();
        }
    }

    @Override
    public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
        if (System.currentTimeMillis() - lastSendTime >= MAX_REFRESH_INTERVAL) {
            lastSendTime = System.currentTimeMillis();
            screenshotHandler.post(this::dispatchScreenshot);
        } else {
            if (changedEvent.getStateType() == ViewStateChangedEvent.StateType.MANUAL_CHANGED) {
                refreshScreenshot(EVENT_REFRESH_INTERVAL);
            } else {
                refreshScreenshot();
            }
        }
    }

    private void dispatchScreenshot() {
        if (refreshListener == null) return;
        Activity activity = activityStateProvider.getForegroundActivity();
        if (activity == null) return;

        try {
            ScreenshotUtil.getScreenshotBitmap(scale, bitmap -> {
                try {
                    String screenshotBase64 = ScreenshotUtil.getScreenshotBase64(bitmap);
                    sendScreenshotRefreshed(screenshotBase64, scale);
                } catch (IOException e) {
                    Logger.e(TAG, "base64 screenshot failed:" + e.getMessage());
                }
            });
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, "dispatch screenshot failed:" + e.getMessage());
        }
    }

    private void refreshScreenshot(long duration) {
        screenshotHandler.removeCallbacks(refreshScreenshotRunnable);
        screenshotHandler.postDelayed(refreshScreenshotRunnable, duration);
    }

    public void refreshScreenshot() {
        refreshScreenshot(MIN_REFRESH_INTERVAL);
    }

    public void registerScreenshotRefreshedListener(OnScreenshotRefreshedListener listener) {
        register();
        this.refreshListener = listener;
        refreshScreenshot();

        registry.executeData(EventFlutter.flutterCircle(true), EventFlutter.class, Void.class);
    }

    public void unregisterScreenshotRefreshedListener() {
        this.refreshListener = null;
        unRegister();

        registry.executeData(EventFlutter.flutterCircle(false), EventFlutter.class, Void.class);
    }

    @Override
    public boolean enableManualState() {
        return registry.getModelLoader(EventFlutter.class) == null;
    }

    public interface OnScreenshotRefreshedListener {
        void onScreenshotRefreshed(CircleScreenshot screenshot);
    }

    private long mSnapshotKey = 0;

    private void sendScreenshotRefreshed(String screenshotBase64, float scale) {
        lastSendTime = System.currentTimeMillis();
        CircleScreenshot.Builder builder = new CircleScreenshot
                .Builder(deviceInfoProvider.getScreenWidth(), deviceInfoProvider.getScreenHeight())
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++);

        builder.buildWithNodes(viewNodeProvider, new Callback<CircleScreenshot>() {
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
        if (result != null && refreshListener != null) {
            this.refreshListener.onScreenshotRefreshed(result);
        }
    }

    public void generateCircleData(Circler.CirclerData data) {
        if (refreshListener == null) return;
        screenshotHandler.removeMessages(0);
        Message message = Message.obtain(screenshotHandler, new CircleDataThread(data));
        message.what = 0;
        screenshotHandler.sendMessageDelayed(message, 100L);
    }

    private class CircleDataThread implements Runnable {
        final Circler.CirclerData circlerData;

        CircleDataThread(Circler.CirclerData circlerData) {
            this.circlerData = circlerData;
        }

        @Override
        public void run() {
            lastSendTime = System.currentTimeMillis();

            CircleScreenshot.Builder builder = new CircleScreenshot.Builder((int) circlerData.getWidth(), (int) circlerData.getHeight())
                    .setScale((float) circlerData.getScale())
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
                    ScreenshotUtil.getScreenshotBitmap(scale, bitmap -> {
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
        builder.buildWithNodes(viewNodeProvider, new Callback<CircleScreenshot>() {
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

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            safeTipView.show(event.getActivity());
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            safeTipView.removeOnly();
        }
        super.onActivityLifecycle(event);
    }

    void enableTipViewShow() {
        safeTipView.enableShow();
    }

    void disableTipView() {
        safeTipView.dismiss();
    }

    void readyTipView(ThreadSafeTipView.OnExitListener listener) {
        safeTipView.onReady(listener);
    }

    void setTipViewMessage(int message) {
        safeTipView.setErrorMessage(message);
    }

    void showQuitDialog(ThreadSafeTipView.OnExitListener listener) {
        safeTipView.showQuitedDialog(listener);
    }

    static final String MSG_READY_TYPE = "ready";
    static final String MSG_QUIT_TYPE = "quit";
    private static final String MSG_CLIENT_TYPE = "client_info";

    String buildReadyMessage() {
        CoreConfiguration core = configurationProvider.core();
        JSONObject json = new JSONObject();
        try {
            json.put("projectId", core.getProjectId());
            json.put("msgType", MSG_READY_TYPE);
            json.put("timestamp", System.currentTimeMillis());
            json.put("domain", appInfoProvider.getPackageName());
            json.put("sdkVersion", SDKConfig.SDK_VERSION);
            json.put("sdkVersionCode", SDKConfig.SDK_VERSION_CODE);
            json.put("os", deviceInfoProvider.getPlatform());
            json.put("screenWidth", deviceInfoProvider.getScreenWidth());
            json.put("screenHeight", deviceInfoProvider.getScreenHeight());
            json.put("urlScheme", core.getUrlScheme());
        } catch (JSONException ignored) {
        }
        return json.toString();
    }

    String buildQuitMessage() {
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", MSG_QUIT_TYPE);
        } catch (JSONException ignored) {
        }
        return json.toString();
    }

    String buildClientInfoMessage() {
        CoreConfiguration core = configurationProvider.core();
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", MSG_CLIENT_TYPE);
            json.put("sdkVersion", SDKConfig.SDK_VERSION);

            JSONObject info = new JSONObject();
            info.put("os", deviceInfoProvider.getPlatform());
            info.put("appVersion", appInfoProvider.getAppVersion());
            info.put("appChannel", core.getChannel());
            info.put("osVersion", deviceInfoProvider.getPlatformVersion());
            info.put("deviceType", deviceInfoProvider.getDeviceType());
            info.put("deviceBrand", deviceInfoProvider.getDeviceBrand());
            info.put("deviceModel", deviceInfoProvider.getDeviceModel());

            json.put("data", info);

        } catch (JSONException ignored) {
        }

        return json.toString();
    }

}