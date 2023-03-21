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

package com.growingio.android.sdk.track.providers;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.log.Logger;

/**
 * <p>
 * cache events before sdk init
 *
 * @author cpacm 2023/3/21
 */
public class CacheEventProvider {
    private static class SingleInstance {
        private static final CacheEventProvider INSTANCE = new CacheEventProvider();
    }

    private CacheEventProvider() {
    }

    private final CircularFifoQueue<BaseEvent.BaseBuilder<?>> caches = new CircularFifoQueue<>(100);

    public static CacheEventProvider get() {
        return CacheEventProvider.SingleInstance.INSTANCE;
    }

    public void releaseCaches() {
        if (caches.size() > 0 && TrackerContext.initializedSuccessfully() && ConfigurationProvider.core().isDataCollectionEnabled()) {
            for (BaseEvent.BaseBuilder<?> eventBuilder : caches) {
                TrackMainThread.trackMain().postEventToTrackMain(eventBuilder);
            }
            caches.clear();
        } else {
            // drop events if data collect disabled
            caches.clear();
        }
    }

    public void cacheEvent(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (!TrackerContext.initializedSuccessfully()) {
            caches.add(eventBuilder);
        } else {
            TrackMainThread.trackMain().postEventToTrackMain(eventBuilder);
        }
    }

}
