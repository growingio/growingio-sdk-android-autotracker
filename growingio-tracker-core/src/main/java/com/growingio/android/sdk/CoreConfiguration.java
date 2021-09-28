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

package com.growingio.android.sdk;

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.helper.EventExcludeFilter;
import com.growingio.android.sdk.track.events.helper.FieldIgnoreFilter;
import com.growingio.android.sdk.track.utils.ObjectUtils.FieldToString;

import java.util.ArrayList;
import java.util.List;

public class CoreConfiguration implements Configurable {
    private String mProjectId;
    private String mUrlScheme;

    private String mChannel;
    private boolean mDebugEnabled = false;
    private int mCellularDataLimit = 10;
    private int mDataUploadInterval = 15;
    private int mSessionInterval = 30;
    private boolean mDataCollectionEnabled = true;
    private boolean mUploadExceptionEnabled = true;
    private String mDataCollectionServerHost = "http://api.growingio.com";
    @FieldToString(clazz = EventExcludeFilter.class, method = "getFilterEventLog", parameterTypes = {int.class}, returnType = String.class)
    private int mExcludeEventFlag = EventExcludeFilter.NONE;
    @FieldToString(clazz = FieldIgnoreFilter.class, method = "getFieldFilterLog", parameterTypes = {int.class}, returnType = String.class)
    private int mIgnoreFieldFlag = FieldIgnoreFilter.NONE;
    private final List<LibraryGioModule> mComponents = new ArrayList<>();
    private boolean mIdMappingEnabled = false;

    public CoreConfiguration(String projectId, String urlScheme) {
        mProjectId = projectId;
        mUrlScheme = urlScheme;
    }

    public CoreConfiguration setProject(String projectId, String urlScheme) {
        mProjectId = projectId;
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

    public boolean isUploadExceptionEnabled() {
        return mUploadExceptionEnabled;
    }

    public CoreConfiguration setUploadExceptionEnabled(boolean uploadExceptionEnabled) {
        this.mUploadExceptionEnabled = uploadExceptionEnabled;
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

    public CoreConfiguration setExcludeEvent(@EventExcludeFilter.EventFilterLimit int filterEventFlag) {
        if (filterEventFlag == EventExcludeFilter.NONE) {
            this.mExcludeEventFlag = EventExcludeFilter.NONE;
        } else {
            this.mExcludeEventFlag |= filterEventFlag;
        }
        return this;
    }

    public int getExcludeEvent() {
        return mExcludeEventFlag;
    }

    public CoreConfiguration setIgnoreField(@FieldIgnoreFilter.FieldFilterType int ignoreFieldFlag) {
        if (ignoreFieldFlag == FieldIgnoreFilter.NONE) {
            this.mIgnoreFieldFlag = FieldIgnoreFilter.NONE;
        } else {
            this.mIgnoreFieldFlag |= ignoreFieldFlag;
        }
        return this;
    }

    public int getIgnoreField() {
        return mIgnoreFieldFlag;
    }

    public CoreConfiguration setPreloadComponent(LibraryGioModule component) {
        if (component == null) {
            return this;
        }

        mComponents.add(component);
        return this;
    }

    public List<LibraryGioModule> getPreoloadComponents() {
        return mComponents;
    }
  
    public boolean isIdMappingEnabled() {
        return mIdMappingEnabled;
    }

    public CoreConfiguration setIdMappingEnabled(boolean enabled) {
        this.mIdMappingEnabled = enabled;
        return (CoreConfiguration) this;
    }
  
}
