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

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * <p>
 * debug event wrapper for debugger service
 *
 * @author cpacm 2021/2/24
 */
public class DebuggerEventWrapper implements EventBuildInterceptor {

    private OnDebuggerEventListener onDebuggerEventListener;

    public DebuggerEventWrapper(OnDebuggerEventListener listener) {
        this.onDebuggerEventListener = listener;
    }

    @Override
    public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
        eventBuilder.build().toJSONObject();
    }

    @Override
    public void eventDidBuild(GEvent event) {
        if (event instanceof BaseEvent) {
            BaseEvent baseEvent = (BaseEvent) event;
            //TODO 修饰event
            JSONObject eventJson = baseEvent.toJSONObject();

            if (onDebuggerEventListener != null) {
                onDebuggerEventListener.onDebuggerEvent(eventJson.toString());
            }
        }
    }



    public void ready() {
        TrackMainThread.trackMain().addEventBuildInterceptor(this);
    }

    public void end() {
        TrackMainThread.trackMain().removeEventBuildInterceptor(this);
    }

    public interface OnDebuggerEventListener {
        void onDebuggerEvent(String eventJson);
    }

}
