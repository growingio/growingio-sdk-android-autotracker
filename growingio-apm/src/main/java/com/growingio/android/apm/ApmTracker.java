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
package com.growingio.android.apm;

import com.growingio.android.gmonitor.ITracker;
import com.growingio.android.gmonitor.event.Breadcrumb;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;

/**
 * <p>
 * if sdk doesn't init or enable,push breadcrumb into fifo queue
 *
 * @author cpacm 2022/9/27
 */
class ApmTracker implements ITracker {

    @Override
    public void trackBreadcrumb(Breadcrumb breadcrumb) {
        if (breadcrumb == null) return;

        CustomEvent.Builder builder = ApmEventBuilder.filterWithApmBreadcrumb(breadcrumb);
        if (breadcrumb.getType().equals(Breadcrumb.TYPE_ERROR)) {
            // 遇到异常时，不能通过Handler保存事件.
            TrackMainThread.trackMain().cacheEventSync(builder);
        } else {
            TrackMainThread.trackMain().cacheEventToTrackMain(builder);
        }
    }

    @Override
    public ITracker clone() {
        return new ApmTracker();
    }
}
