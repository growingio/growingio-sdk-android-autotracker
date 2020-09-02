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

package com.growingio.android.sdk.autotrack;

import androidx.annotation.NonNull;

import com.growingio.android.sdk.track.TrackConfiguration;

public class AutotrackConfiguration extends TrackConfiguration {

    private float mImpressionScale = 0;

    public AutotrackConfiguration setImpressionScale(float scale) {
        this.mImpressionScale = scale;
        return this;
    }

    public float getImpressionScale() {
        return mImpressionScale;
    }

    @Override
    public AutotrackConfiguration setProjectId(String projectId) {
        super.setProjectId(projectId);
        return this;
    }

    @Override
    public AutotrackConfiguration setUrlScheme(String urlScheme) {
        super.setUrlScheme(urlScheme);
        return this;
    }

    @Override
    public AutotrackConfiguration setChannel(String channel) {
        super.setChannel(channel);
        return this;
    }

    @Override
    public AutotrackConfiguration setUploadExceptionEnabled(boolean uploadExceptionEnabled) {
        super.setUploadExceptionEnabled(uploadExceptionEnabled);
        return this;
    }

    @Override
    public AutotrackConfiguration setDebugEnabled(boolean enabled) {
        super.setDebugEnabled(enabled);
        return this;
    }

    @Override
    public AutotrackConfiguration setCellularDataLimit(int cellularDataLimit) {
        super.setCellularDataLimit(cellularDataLimit);
        return this;
    }

    @Override
    public AutotrackConfiguration setDataUploadInterval(long dataUploadInterval) {
        super.setDataUploadInterval(dataUploadInterval);
        return this;
    }

    @Override
    public AutotrackConfiguration setSessionInterval(long sessionInterval) {
        super.setSessionInterval(sessionInterval);
        return this;
    }

    @Override
    public AutotrackConfiguration setDataCollectionEnabled(boolean dataCollectionEnabled) {
        super.setDataCollectionEnabled(dataCollectionEnabled);
        return this;
    }

    @Override
    public AutotrackConfiguration setDataCollectionServerHost(String dataCollectionServerHost) {
        super.setDataCollectionServerHost(dataCollectionServerHost);
        return this;
    }

    @NonNull
    @Override
    public AutotrackConfiguration clone() {
        return new AutotrackConfiguration()
                .setProjectId(getProjectId())
                .setUrlScheme(getUrlScheme())
                .setChannel(getChannel())
                .setDebugEnabled(isDebugEnabled())
                .setCellularDataLimit(getCellularDataLimit())
                .setDataUploadInterval(getDataUploadInterval())
                .setSessionInterval(getSessionInterval())
                .setUploadExceptionEnabled(isUploadExceptionEnabled())
                .setImpressionScale(getImpressionScale())
                .setDataCollectionServerHost(getDataCollectionServerHost());
    }
}
