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
