package com.growingio.android.analytics.google;

import com.growingio.android.sdk.Configurable;

import java.util.HashMap;
import java.util.Map;

public class GoogleAnalyticsConfiguration implements Configurable {

    private Map<String, String> mDatasourceIds = new HashMap<>();
    private String mCollectId;
    private String mServerHost;

    public GoogleAnalyticsConfiguration setDatasourceIds(Map<String, String> map) {
        if (map != null) {
            this.mDatasourceIds = map;
        }
        return this;
    }

    public Map<String, String> getDatasourceIds() {
        return this.mDatasourceIds;
    }

    public GoogleAnalyticsConfiguration setCollectId(String collectId) {
        mCollectId = collectId;
        return this;
    }

    public String getCollectId() {
        return mCollectId;
    }

    public GoogleAnalyticsConfiguration setServerHost(String serverHost) {
        mServerHost = serverHost;
        return this;
    }

    public String getServerHost() {
        return mServerHost;
    }
}
