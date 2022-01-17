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

package com.growingio.android.sdk.track;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.listener.OnUserIdChangedListener;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

public class CdpEventBuildInterceptor implements EventBuildInterceptor, OnUserIdChangedListener {
    private static final String TAG = "CdpEventBuildInterceptor";

    private static final String KEY_GIO_ID = "GIO_ID";

    private final String mDataSourceId;

    public CdpEventBuildInterceptor(String dataSourceId) {
        mDataSourceId = dataSourceId;
        UserInfoProvider.get().registerUserIdChangedListener(this);
    }

    @Override
    public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
        eventBuilder.addExtraParam("dataSourceId", mDataSourceId);
        String mLatestGioId = getGioId();
        if (!TextUtils.isEmpty(mLatestGioId)) {
            eventBuilder.addExtraParam("gioId", mLatestGioId);
        }
    }

    @Override
    public void eventDidBuild(GEvent event) {

    }

    private String getGioId() {
        return PersistentDataProvider.get().getString(KEY_GIO_ID, null);
    }

    private void setGioId(String gioId) {
        PersistentDataProvider.get().putString(KEY_GIO_ID, gioId);
    }

    @Override
    public void onUserIdChanged(@Nullable String newUserId) {
        String mLatestGioId = getGioId();
        Logger.d(TAG, "onUserIdChanged: newUserId = " + newUserId + ", mLatestGioId = " + mLatestGioId);
        if (newUserId != null && newUserId.length() != 0 && !newUserId.equals(mLatestGioId)) {
            setGioId(newUserId);
        }
    }
}
