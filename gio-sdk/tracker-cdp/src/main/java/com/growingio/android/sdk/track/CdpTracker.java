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

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.CallSuper;

import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.track.events.cdp.ResourceItem;
import com.growingio.android.sdk.track.events.cdp.ResourceItemCustomEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public class CdpTracker extends Tracker {
    private static final String TAG = "CdpTracker";

    public CdpTracker(Application application) {
        super(application);
    }

    @Override
    @CallSuper
    protected void setup(Application application) {
        super.setup(application);
        CdpConfig cdpConfig = ConfigurationProvider.get().getConfiguration(CdpConfig.class);
        if (cdpConfig != null) {
            TrackMainThread.trackMain().addEventBuildInterceptor(new CdpEventBuildInterceptor(cdpConfig.getDataSourceId()));
        }
    }

    public void trackCustomEvent(String eventName, String itemKey, String itemId) {
        if (!isInited) return;
        trackCustomEvent(eventName, null, itemKey, itemId);
    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes, String itemKey, String itemId) {
        if (!isInited) return;
        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(itemKey) || TextUtils.isEmpty(itemId)) {
            Logger.e(TAG, "trackCustomEvent: eventName, itemKey or itemId is NULL");
            return;
        }

        if (attributes != null) {
            attributes = new HashMap<>(attributes);
        }

        TrackMainThread.trackMain().postEventToTrackMain(
                new ResourceItemCustomEvent.Builder()
                        .setEventName(eventName)
                        .setAttributes(attributes)
                        .setResourceItem(new ResourceItem(itemKey, itemId))
        );
    }
}
