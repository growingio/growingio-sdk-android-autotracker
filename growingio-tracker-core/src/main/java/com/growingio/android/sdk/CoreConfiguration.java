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
package com.growingio.android.sdk;

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.EventFilterInterceptor;

import java.util.ArrayList;
import java.util.List;

public class CoreConfiguration implements Configurable {
    private String mProjectId;
    private String mUrlScheme;
    private String mDataSourceId;
    private String mChannel;
    private boolean mDebugEnabled = false;
    private int mCellularDataLimit = 20;
    private int mDataUploadInterval = 15;
    private int mSessionInterval = 30;
    private boolean mDataCollectionEnabled = true;

    private boolean mCustomEventWithPage = false;
    private boolean mRequireAppProcessesEnabled = false;
    private String mDataCollectionServerHost = "https://napi.growingio.com";
    private EventFilterInterceptor mEventFilterInterceptor;
    private final List<LibraryGioModule> mComponents = new ArrayList<>();
    private boolean mIdMappingEnabled = false;

    private boolean mImeiEnabled = false;
    private boolean mAndroidIdEnabled = false;

    private int mDataValidityPeriod = 7;

    public CoreConfiguration(String accountId, String urlScheme) {
        mProjectId = accountId;
        mUrlScheme = urlScheme;
    }

    public CoreConfiguration setProject(String accountId, String urlScheme) {
        mProjectId = accountId;
        mUrlScheme = urlScheme;
        return this;
    }

    public String getProjectId() {
        return mProjectId;
    }

    public boolean isDataCollectionEnabled() {
        return mDataCollectionEnabled;
    }

    public CoreConfiguration setDataCollectionEnabled(boolean dataCollectionEnabled) {
        mDataCollectionEnabled = dataCollectionEnabled;
        return this;
    }

    public String getUrlScheme() {
        return mUrlScheme;
    }

    public String getChannel() {
        return mChannel;
    }

    public CoreConfiguration setChannel(String channel) {
        this.mChannel = channel;
        return this;
    }

    public String getDataSourceId() {
        return mDataSourceId;
    }

    /**
     * 设置dataSourceId
     *
     * @param dataSourceId 设置APP的DataSourceId
     * @return this
     */
    public CoreConfiguration setDataSourceId(String dataSourceId) {
        mDataSourceId = dataSourceId;
        return this;
    }

    public boolean isDebugEnabled() {
        return mDebugEnabled;
    }

    public CoreConfiguration setDebugEnabled(boolean enabled) {
        this.mDebugEnabled = enabled;
        return this;
    }

    public int getCellularDataLimit() {
        return mCellularDataLimit;
    }

    public CoreConfiguration setCellularDataLimit(int cellularDataLimit) {
        this.mCellularDataLimit = cellularDataLimit;
        return this;
    }

    public int getDataUploadInterval() {
        return mDataUploadInterval;
    }

    public CoreConfiguration setDataUploadInterval(int dataUploadInterval) {
        this.mDataUploadInterval = dataUploadInterval;
        return this;
    }

    public int getSessionInterval() {
        return mSessionInterval;
    }

    public CoreConfiguration setSessionInterval(int sessionInterval) {
        this.mSessionInterval = sessionInterval;
        return this;
    }

    public String getDataCollectionServerHost() {
        return mDataCollectionServerHost;
    }

    public CoreConfiguration setDataCollectionServerHost(String dataCollectionServerHost) {
        if (!TextUtils.isEmpty(dataCollectionServerHost)) {
            mDataCollectionServerHost = dataCollectionServerHost;
        }
        return this;
    }

    public EventFilterInterceptor getEventFilterInterceptor() {
        return mEventFilterInterceptor;
    }

    public CoreConfiguration setEventFilterInterceptor(EventFilterInterceptor eventFilterInterceptor) {
        this.mEventFilterInterceptor = eventFilterInterceptor;
        return this;
    }

    public CoreConfiguration addPreloadComponent(LibraryGioModule component) {
        if (component == null) {
            return this;
        }
        mComponents.add(component);
        return this;
    }

    protected List<LibraryGioModule> getPreloadComponents() {
        return mComponents;
    }

    public boolean isIdMappingEnabled() {
        return mIdMappingEnabled;
    }

    public CoreConfiguration setIdMappingEnabled(boolean enabled) {
        this.mIdMappingEnabled = enabled;
        return this;
    }

    public boolean isRequireAppProcessesEnabled() {
        return mRequireAppProcessesEnabled;
    }

    public CoreConfiguration setRequireAppProcessesEnabled(boolean enabled) {
        this.mRequireAppProcessesEnabled = enabled;
        return this;
    }

    public boolean isImeiEnabled() {
        return mImeiEnabled;
    }

    public CoreConfiguration setImeiEnabled(boolean imeiEnabled) {
        this.mImeiEnabled = imeiEnabled;
        return this;
    }

    public boolean isAndroidIdEnabled() {
        return mAndroidIdEnabled;
    }

    public CoreConfiguration setAndroidIdEnabled(boolean androidIdEnabled) {
        this.mAndroidIdEnabled = androidIdEnabled;
        return this;
    }

    public int getDataValidityPeriod() {
        return mDataValidityPeriod;
    }

    /**
     * Sets the cache data validity period. From 3 days to 30 days.
     * <p> Default: 7 days.
     *
     * @param dataValidityPeriod data validity period, in days. for example, 7 means that the cache data is valid for 7 days.
     */
    public CoreConfiguration setDataValidityPeriod(int dataValidityPeriod) {
        this.mDataValidityPeriod = dataValidityPeriod;
        return this;
    }

    /**
     * Bring page path to custom event. If called, the custom event will be associated with the page path.
     */
    public CoreConfiguration embedPathInCustomEvent() {
        this.mCustomEventWithPage = true;
        return this;
    }

    public boolean isCustomEventWithPage() {
        return mCustomEventWithPage;
    }
}
