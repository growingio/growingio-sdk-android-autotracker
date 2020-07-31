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

package com.growingio.android.sdk.track.providers;

import com.growingio.android.sdk.track.middleware.IEventSender;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.variation.EventHttpSender;
import com.growingio.android.sdk.track.variation.TrackEventJsonMarshaller;

public interface EventSenderProvider {
    void registerEventSender(IEventSender sender);

    IEventSender getEventSender();

    class EventSenderPolicy implements EventSenderProvider {
        private IEventSender mSender;

        public static EventSenderProvider get() {
            return GIOProviders.provider(EventSenderPolicy.class, new GIOProviders.DefaultCallback<EventSenderPolicy>() {
                @Override
                public EventSenderPolicy value() {
                    return new EventSenderPolicy();
                }
            });
        }

        @Override
        public void registerEventSender(IEventSender sender) {
            mSender = sender;
        }

        @Override
        public IEventSender getEventSender() {
            if (mSender == null) {
                mSender = new EventHttpSender(new TrackEventJsonMarshaller());
            }
            return mSender;
        }
    }
}
