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
package com.growingio.android.debugger;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;

/**
 * <p>
 * debug event wrapper for debugger service
 *
 * @author cpacm 2021/2/24
 */
public class DebuggerEventProvider implements EventBuildInterceptor, TrackerLifecycleProvider {
    private static final String TAG = "DebuggerEventProvider";

    public static final String SERVICE_LOGGER_OPEN = "logger_open";
    public static final String SERVICE_LOGGER_CLOSE = "logger_close";
    public static final String SERVICE_DEBUGGER_TYPE = "debugger_data";

    private OnDebuggerEventListener debuggerEventListener;
    private EventBuilderProvider eventBuilderProvider;
    private ConfigurationProvider configurationProvider;


    DebuggerEventProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        eventBuilderProvider = context.getProvider(EventBuilderProvider.class);
        // before debugger start, cache events.
        eventBuilderProvider.addEventBuildInterceptor(this);
    }

    @Override
    public void shutdown() {
        eventBuilderProvider.removeEventBuildInterceptor(this);
    }

    public void registerDebuggerEventListener(OnDebuggerEventListener listener) {
        this.debuggerEventListener = listener;
    }

    public void ready() {
        mIsConnected = true;
        eventBuilderProvider.addEventBuildInterceptor(this);
        sendCacheMessage();
    }

    public void end() {
        mIsConnected = false;
        eventBuilderProvider.removeEventBuildInterceptor(this);
        closeLogger();
        debuggerEventListener = null;
    }

    /***************** Base Event *******************/
    private volatile boolean mIsConnected = false;
    private final Queue<String> mCollectionMessage = new CircularFifoQueue<>(50);

    private void sendCacheMessage() {
        for (String message : mCollectionMessage) {
            if (debuggerEventListener != null) {
                debuggerEventListener.onDebuggerMessage(message);
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
                JSONObject eventJson = EventBuilderProvider.toJson(baseEvent);
                //添加额外的url,以便debugger显示请求地址
                eventJson.put("url", getUrl());
                JSONObject json = new JSONObject();
                json.put("msgType", SERVICE_DEBUGGER_TYPE);
                json.put("sdkVersion", SDKConfig.SDK_VERSION);
                json.put("data", eventJson);
                if (mIsConnected && debuggerEventListener != null) {
                    debuggerEventListener.onDebuggerMessage(json.toString());
                } else {
                    mCollectionMessage.add(json.toString());
                }

            } catch (JSONException ignored) {
                Logger.e("DebuggerEventWrapper", "can't get event json " + event.getEventType());
            }
        }
    }

    private String getUrl() {
        StringBuilder url = new StringBuilder(configurationProvider.core().getDataCollectionServerHost());
        if (url.length() > 0 && url.charAt(url.length() - 1) != '/') {
            url.append("/");
        }
        url.append("v3/projects/");
        String projectId = configurationProvider.core().getProjectId();
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
            if (debuggerEventListener != null) {
                debuggerEventListener.onDebuggerMessage(logMessage);
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

    public interface OnDebuggerEventListener {
        void onDebuggerMessage(String message);
    }

}
