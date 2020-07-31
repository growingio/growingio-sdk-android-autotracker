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

package com.growingio.android.sdk.autotrack.providers;

import androidx.annotation.UiThread;

import com.growingio.android.sdk.autotrack.events.PageAttributesEvent;
import com.growingio.android.sdk.autotrack.events.PageEvent;
import com.growingio.android.sdk.track.GIOMainThread;
import com.growingio.android.sdk.track.GInternal;
import com.growingio.android.sdk.track.utils.GIOProviders;

import java.util.Map;

public interface EventAutotrackGeneratorProvider {
    @UiThread
    void generatePageEvent(String pageName, String title, long timestamp);

    void generatePageAttributesEvent(String pageName, long pageShowTimestamp, Map<String, String> attributes);


    class EventAutotrackGenerator implements EventAutotrackGeneratorProvider {

        public static EventAutotrackGeneratorProvider get() {
            return GIOProviders.provider(EventAutotrackGeneratorProvider.class, new GIOProviders.DefaultCallback<EventAutotrackGeneratorProvider>() {
                @Override
                public EventAutotrackGeneratorProvider value() {
                    return new EventAutotrackGenerator();
                }
            });
        }

        @Override
        public void generatePageEvent(String pageName, String title, long timestamp) {
            GIOMainThread mainThread = GInternal.getInstance().getMainThread();
            if (mainThread != null) {
                mainThread.postEventToGMain(
                        new PageEvent.EventBuilder(mainThread.getCoreAppState())
                                .setPageName(pageName)
                                .setTitle(title)
                                .setTimestamp(timestamp)
                );
            }
        }

        @Override
        public void generatePageAttributesEvent(String pageName, long pageShowTimestamp, Map<String, String> attributes) {
            GIOMainThread mainThread = GInternal.getInstance().getMainThread();
            if (mainThread != null) {
                mainThread.postEventToGMain(
                        new PageAttributesEvent.EventBuilder(mainThread.getCoreAppState())
                                .setPageName(pageName)
                                .setPageShowTimestamp(pageShowTimestamp)
                                .setAttributes(attributes));
            }
        }
    }
}
