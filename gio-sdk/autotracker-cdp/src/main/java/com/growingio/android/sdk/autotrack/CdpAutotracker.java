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

import android.app.Application;
import android.text.TextUtils;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.cdp.CdpEventBuildInterceptor;
import com.growingio.android.sdk.track.cdp.ResourceItem;
import com.growingio.android.sdk.track.cdp.ResourceItemCustomEvent;
import com.growingio.android.sdk.track.log.Logger;

import java.util.HashMap;
import java.util.Map;

public class CdpAutotracker extends Autotracker {
    private static final String TAG = "GrowingAutotracker";

    public CdpAutotracker(Application application, CdpAutotrackConfiguration trackConfiguration) {
        super(application, trackConfiguration);
        if (trackConfiguration != null) {
            TrackMainThread.trackMain().addEventBuildInterceptor(new CdpEventBuildInterceptor(trackConfiguration.getDataSourceId()));
        }
    }

    public void trackCustomEvent(String eventName, String itemKey, String itemId) {
        if (!isInited) return;
        trackCustomEvent(eventName, null, itemKey, itemId);
    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes, String itemKey, String itemId) {
        if (!isInited) return;
        if (TextUtils.isEmpty(itemKey) || TextUtils.isEmpty(itemId)) {
            Logger.e(TAG, "trackCustomEvent: itemKey or itemId is NULL");
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
