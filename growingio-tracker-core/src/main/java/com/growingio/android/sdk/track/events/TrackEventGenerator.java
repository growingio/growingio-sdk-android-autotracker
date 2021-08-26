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

package com.growingio.android.sdk.track.events;

import com.growingio.android.sdk.track.TrackMainThread;

import java.util.Map;

public class TrackEventGenerator {
    private TrackEventGenerator() {
    }

    public static void generateVisitEvent() {
        TrackMainThread.trackMain().postEventToTrackMain(
                new VisitEvent.Builder()
        );
    }

    public static void generateCustomEvent(String name, Map<String, String> attributes) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new CustomEvent.Builder()
                        .setEventName(name)
                        .setAttributes(attributes)
        );
    }

    public static void generateConversionVariablesEvent(Map<String, String> variables) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new ConversionVariablesEvent.Builder()
                        .setAttributes(variables)
        );
    }

    public static void generateLoginUserAttributesEvent(Map<String, String> attributes) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new LoginUserAttributesEvent.Builder()
                        .setAttributes(attributes)
        );
    }

    public static void generateVisitorAttributesEvent(Map<String, String> attributes) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new VisitorAttributesEvent.Builder()
                        .setAttributes(attributes)
        );
    }

    public static void generateAppClosedEvent() {
        TrackMainThread.trackMain().postEventToTrackMain(
                new AppClosedEvent.Builder()
        );
    }
}
