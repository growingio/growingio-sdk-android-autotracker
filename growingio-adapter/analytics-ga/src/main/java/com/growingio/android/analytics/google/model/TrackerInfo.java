/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.analytics.google.model;

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.EventBuildInterceptor;

import java.util.HashMap;
import java.util.Map;

public class TrackerInfo {
    private String mSessionId;
    private String mDataSourceId;
    private String mUserId;
    private String mLastUserId;
    private Map<String, String> extraParams;
    private EventBuildInterceptor mEventBuildInterceptor;

    public TrackerInfo(String datasourceId, String sessionId) {
        this.mDataSourceId = datasourceId;
        this.mSessionId = sessionId;
        this.extraParams = new HashMap<>();
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setSessionId(String sessionId) {
        this.mSessionId = sessionId;
    }

    public String getDatasourceId() {
        return mDataSourceId;
    }

    public void setDatasourceId(String datasource) {
        this.mDataSourceId = datasource;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getLastUserId() {
        return mLastUserId;
    }

    public void setLastUserId(String mLastUserId) {
        this.mLastUserId = mLastUserId;
    }

    public EventBuildInterceptor getEventBuildInterceptor() {
        return mEventBuildInterceptor;
    }

    public void setEventBuildInterceptor(EventBuildInterceptor mEventBuildInterceptor) {
        this.mEventBuildInterceptor = mEventBuildInterceptor;
    }

    public void addParam(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (value == null) {
            extraParams.remove(key);
            return;
        }
        extraParams.put(key, value);
    }

    public Map<String, String> getParams() {
        return extraParams;
    }
}
