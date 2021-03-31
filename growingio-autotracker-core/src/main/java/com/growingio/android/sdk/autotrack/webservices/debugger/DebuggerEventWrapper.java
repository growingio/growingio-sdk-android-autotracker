/*
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.sdk.autotrack.webservices.debugger;

import com.growingio.android.sdk.autotrack.webservices.ScreenshotProvider;
import com.growingio.android.sdk.autotrack.webservices.circle.entity.CircleScreenshot;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.webservices.log.WsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;

/**
 * <p>
 * debug event wrapper for debugger service
 *
 * @author cpacm 2021/2/24
 */
public class DebuggerEventWrapper implements EventBuildInterceptor, ScreenshotProvider.OnScreenshotRefreshedListener {
    private static final String TAG = "DebuggerEventWrapper";

    public static final String SERVICE_LOGGER_OPEN = "logger_open";
    public static final String SERVICE_LOGGER_CLOSE = "logger_close";
    public static final String SERVICE_DEBUGGER_TYPE = "debugger_data";

    private OnDebuggerEventListener mOnDebuggerEventListener;

    private static class SingleInstance {
        private static final DebuggerEventWrapper INSTANCE = new DebuggerEventWrapper();
    }

    public static DebuggerEventWrapper get() {
        return DebuggerEventWrapper.SingleInstance.INSTANCE;
    }

    public void registerDebuggerEventListener(OnDebuggerEventListener listener) {
        this.mOnDebuggerEventListener = listener;
    }

    private DebuggerEventWrapper() {
    }

    public void observeEventBuild() {
        TrackMainThread.trackMain().addEventBuildInterceptor(this);
    }

    public void ready() {
        mIsConnected = true;
        ScreenshotProvider.get().registerScreenshotRefreshedListener(this);
        sendCacheMessage();
    }

    public void end() {
        mIsConnected = false;
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(this);
        TrackMainThread.trackMain().removeEventBuildInterceptor(this);
        closeLogger();
    }

    /***************** Base Event *******************/
    private volatile boolean mIsConnected = false;
    private final Queue<String> mCollectionMessage = new CircularFifoQueue<>(50);

    private void sendCacheMessage() {
        for (String message : mCollectionMessage) {
            if (mOnDebuggerEventListener != null) {
                mOnDebuggerEventListener.onDebuggerMessage(message);
            }
        }
        mCollectionMessage.clear();
    }

    @Override
    public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
    }

    @Override
    public void eventDidBuild(GEvent event) {
        if (event instanceof BaseEvent) {
            BaseEvent baseEvent = (BaseEvent) event;
            try {
                JSONObject eventJson = baseEvent.toJSONObject();
                //添加额外的url,以便debugger显示请求地址
                eventJson.put("url", getUrl());
                JSONObject json = new JSONObject();
                json.put("msgType", SERVICE_DEBUGGER_TYPE);
                json.put("sdkVersion", SDKConfig.SDK_VERSION);
                json.put("data", eventJson);
                if (mIsConnected && mOnDebuggerEventListener != null) {
                    mOnDebuggerEventListener.onDebuggerMessage(json.toString());
                } else {
                    mCollectionMessage.add(json.toString());
                }

            } catch (JSONException ignored) {
                Logger.e("DebuggerEventWrapper", "can't get event json " + event.getEventType());
            }
        }
    }

    private String getUrl() {
        StringBuilder url = new StringBuilder(ConfigurationProvider.get().getTrackConfiguration().getDataCollectionServerHost());
        if (url.length() > 0 && url.charAt(url.length() - 1) != '/') {
            url.append("/");
        }
        url.append("v3/projects/");
        String projectId = ConfigurationProvider.get().getTrackConfiguration().getProjectId();
        url.append(projectId);
        url.append("/collect?stm=");
        url.append(System.currentTimeMillis());
        return url.toString();
    }

    /***************** Logger *******************/
    private WsLogger mWsLogger;

    public void printLog() {
        if (mWsLogger != null) {
            mWsLogger.printOut();
        }
    }

    public void openLogger() {
        if (mWsLogger == null) {
            mWsLogger = new WsLogger();
            mWsLogger.openLog();
        }
        mWsLogger.setCallback(logMessage -> {
            if (mOnDebuggerEventListener != null) {
                mOnDebuggerEventListener.onDebuggerMessage(logMessage);
            }
        });
    }

    public void closeLogger() {
        if (mWsLogger == null) {
            return;
        }
        mWsLogger.closeLog();
        mWsLogger.setCallback(null);
        mWsLogger = null;
    }

    /***************** ScreenShot *******************/

    private Disposable mDebuggerScreenshotDisposable;
    private long mSnapshotKey = 0;

    @Override
    public void onScreenshotRefreshed(String screenshotBase64, float scale) {
        if (mDebuggerScreenshotDisposable != null) {
            mDebuggerScreenshotDisposable.dispose();
        }

        mDebuggerScreenshotDisposable = new CircleScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++)
                .build(new Callback<CircleScreenshot>() {
                    @Override
                    public void onSuccess(CircleScreenshot result) {
                        if (mOnDebuggerEventListener != null) {
                            mOnDebuggerEventListener.onDebuggerMessage(result.toJSONObject().toString());
                        }
                        printLog();
                    }

                    @Override
                    public void onFailed() {
                        Logger.e(TAG, "Create debugger screenshot failed");
                    }
                });
    }

    public interface OnDebuggerEventListener {
        void onDebuggerMessage(String message);
    }

}
