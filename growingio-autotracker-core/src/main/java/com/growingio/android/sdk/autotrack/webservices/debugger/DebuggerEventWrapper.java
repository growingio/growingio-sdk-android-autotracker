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

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.webservices.log.WsLogger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * debug event wrapper for debugger service
 *
 * @author cpacm 2021/2/24
 */
public class DebuggerEventWrapper implements EventBuildInterceptor {

    public static final String SERVICE_LOGGER_OPEN = "logger_open";
    public static final String SERVICE_LOGGER_CLOSE = "logger_close";
    public static final String SERVICE_DEBUGGER_TYPE = "debugger_data";

    private final OnDebuggerEventListener onDebuggerEventListener;
    private WsLogger mWsLogger;

    public DebuggerEventWrapper(OnDebuggerEventListener listener) {
        this.onDebuggerEventListener = listener;
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

                if (onDebuggerEventListener != null) {
                    onDebuggerEventListener.onDebuggerEvent(json.toString());
                }
            } catch (JSONException ignored) {
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

    public void openLogger() {
        if (mWsLogger == null) {
            mWsLogger = new WsLogger();
            Logger.addLogger(mWsLogger);
        }
        mWsLogger.setCallback(logMessage -> {
            if (onDebuggerEventListener != null) {
                onDebuggerEventListener.onDebuggerEvent(logMessage.toJSONObject().toString());
            }
        });
    }

    public void closeLogger() {
        if (mWsLogger == null) {
            return;
        }
        mWsLogger.setCallback(null);
        Logger.removeLogger(mWsLogger);
        mWsLogger = null;
    }

    public void ready() {
        TrackMainThread.trackMain().addEventBuildInterceptor(this);
    }

    public void end() {
        TrackMainThread.trackMain().removeEventBuildInterceptor(this);
        closeLogger();
    }

    public interface OnDebuggerEventListener {
        void onDebuggerEvent(String eventJson);
    }

}
