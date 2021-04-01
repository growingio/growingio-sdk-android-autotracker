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

package com.growingio.android.sdk.track;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.growingio.android.sdk.track.base.Configurable;
import com.growingio.android.sdk.track.modelloader.OnTrackerRegistry;

public class TrackConfiguration implements Cloneable, Configurable {
    private final String mProjectId;
    private final String mUrlScheme;

    private String mChannel;
    private boolean mDebugEnabled = false;
    private int mCellularDataLimit = 10;
    private int mDataUploadInterval = 15;
    private int mSessionInterval = 30;
    private boolean mDataCollectionEnabled = true;
    private boolean mUploadExceptionEnabled = true;
    private String mDataCollectionServerHost = "http://api.growingio.com";
    private boolean mOaidEnabled = false;
    private OnTrackerRegistry mRegistryCallback;

    public TrackConfiguration(String projectId, String urlScheme) {
        mProjectId = projectId;
        mUrlScheme = urlScheme;
    }

    public String getProjectId() {
        return mProjectId;
    }

    public boolean isDataCollectionEnabled() {
        return mDataCollectionEnabled;
    }

    public TrackConfiguration setDataCollectionEnabled(boolean dataCollectionEnabled) {
        mDataCollectionEnabled = dataCollectionEnabled;
        return this;
    }

    public String getUrlScheme() {
        return mUrlScheme;
    }

    public String getChannel() {
        return mChannel;
    }

    public TrackConfiguration setChannel(String channel) {
        this.mChannel = channel;
        return this;
    }

    public boolean isUploadExceptionEnabled() {
        return mUploadExceptionEnabled;
    }

    public TrackConfiguration setUploadExceptionEnabled(boolean uploadExceptionEnabled) {
        this.mUploadExceptionEnabled = uploadExceptionEnabled;
        return this;
    }

    public boolean isDebugEnabled() {
        return mDebugEnabled;
    }

    public TrackConfiguration setDebugEnabled(boolean enabled) {
        this.mDebugEnabled = enabled;
        return this;
    }

    public int getCellularDataLimit() {
        return mCellularDataLimit;
    }

    public TrackConfiguration setCellularDataLimit(int cellularDataLimit) {
        this.mCellularDataLimit = cellularDataLimit;
        return this;
    }

    public int getDataUploadInterval() {
        return mDataUploadInterval;
    }

    public TrackConfiguration setDataUploadInterval(int dataUploadInterval) {
        this.mDataUploadInterval = dataUploadInterval;
        return this;
    }

    public int getSessionInterval() {
        return mSessionInterval;
    }

    public TrackConfiguration setSessionInterval(int sessionInterval) {
        this.mSessionInterval = sessionInterval;
        return this;
    }

    public String getDataCollectionServerHost() {
        return mDataCollectionServerHost;
    }

    public TrackConfiguration setDataCollectionServerHost(String dataCollectionServerHost) {
        if (!TextUtils.isEmpty(dataCollectionServerHost)) {
            mDataCollectionServerHost = dataCollectionServerHost;
        }
        return this;
    }

    public boolean isOaidEnabled() {
        return mOaidEnabled;
    }

    public TrackConfiguration setOaidEnabled(boolean enabled) {
        this.mOaidEnabled = enabled;
        return this;
    }

    public TrackConfiguration registerComponents(OnTrackerRegistry registry) {
        this.mRegistryCallback = registry;
        return this;
    }

    public OnTrackerRegistry getRegistryCallback() {
        return mRegistryCallback;
    }

    @NonNull
    @Override
    public TrackConfiguration clone() {
        TrackConfiguration clone = new TrackConfiguration(this.mProjectId, this.mUrlScheme);
        clone.mChannel = this.mChannel;
        clone.mDebugEnabled = this.mDebugEnabled;
        clone.mCellularDataLimit = this.mCellularDataLimit;
        clone.mDataUploadInterval = this.mDataUploadInterval;
        clone.mSessionInterval = this.mSessionInterval;
        clone.mUploadExceptionEnabled = this.mUploadExceptionEnabled;
        clone.mDataCollectionServerHost = this.mDataCollectionServerHost;
        clone.mOaidEnabled = this.mOaidEnabled;
        clone.mRegistryCallback = this.mRegistryCallback;
        return clone;
    }
}
