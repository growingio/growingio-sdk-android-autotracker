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
    private boolean mOaidEnabled = false;
    private int mExcludeEventFlag = EventExcludeFilter.EVENT_MASK_NONE;
    private int mIgnoreFieldFlag = FieldIgnoreFilter.FIELD_IGNORE_NONE;

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

    public boolean isOaidEnabled() {
        return mOaidEnabled;
    }

    public CoreConfiguration setOaidEnabled(boolean enabled) {
        this.mOaidEnabled = enabled;
        return this;
    }

    public CoreConfiguration setExcludeEvent(int filterEventFlag) {
        if (filterEventFlag == EventExcludeFilter.EVENT_MASK_NONE) {
            this.mExcludeEventFlag = EventExcludeFilter.EVENT_MASK_NONE;
        } else {
            this.mExcludeEventFlag |= filterEventFlag;
        }
        return this;
    }

    public int getExcludeEvent() {
        return mExcludeEventFlag;
    }

    public CoreConfiguration setIgnoreField(int ignoreFieldFlag) {
        if (ignoreFieldFlag == FieldIgnoreFilter.FIELD_IGNORE_NONE) {
            this.mIgnoreFieldFlag = FieldIgnoreFilter.FIELD_IGNORE_NONE;
        } else {
            this.mIgnoreFieldFlag |= ignoreFieldFlag;
        }
        return this;
    }

    public int getIgnoreField() {
        return mIgnoreFieldFlag;
    }

}
