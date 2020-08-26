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

package com.growingio.android.sdk.autotrack.events;

import com.growingio.android.sdk.track.TrackMainThread;

import java.util.Map;

public class AutoTrackEventGenerator {
    private AutoTrackEventGenerator() {
    }

    public static void generatePageEvent(String pageName, String title, long timestamp) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageEvent.Builder()
                        .setPageName(pageName)
                        .setTitle(title)
                        .setTimestamp(timestamp)
        );
    }

    public static void generatePageAttributesEvent(String pageName, long pageShowTimestamp, Map<String, String> attributes) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageAttributesEvent.Builder()
                        .setPageName(pageName)
                        .setPageShowTimestamp(pageShowTimestamp)
                        .setAttributes(attributes));
    }
}
