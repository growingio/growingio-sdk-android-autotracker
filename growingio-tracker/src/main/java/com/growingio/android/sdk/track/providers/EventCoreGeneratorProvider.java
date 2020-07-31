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


import android.support.annotation.AnyThread;

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.VisitEvent;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.utils.GIOProviders;

import java.util.Map;

/**
 * 所有事件的生成与postToMain由此类负责
 */
public interface EventCoreGeneratorProvider {

    /**
     * 产生一个访问事件
     */
    @GMainThread
    void generateVisit(String sessionId, long timestamp);

    /**
     * 生成一个自定义打点事件
     */
    @AnyThread
    void generateCustomEvent(String name, Map<String, String> variables);

    @AnyThread
    void generateConversionVariablesEvent(Map<String, String> variables);

    @AnyThread
    void generateLoginUserAttributesEvent(Map<String, String> attributes);


    @AnyThread
    void generateVisitorAttributesEvent(Map<String, String> attributes);

    class EventCoreGenerator implements EventCoreGeneratorProvider {
        private static final String TAG = "GIO.Event";
        private final CoreAppState mCoreAppState;

        public EventCoreGenerator(CoreAppState coreAppState) {
            mCoreAppState = coreAppState;
        }

        public static EventCoreGeneratorProvider get(final CoreAppState coreAppState) {
            return GIOProviders.provider(EventCoreGeneratorProvider.class, new GIOProviders.DefaultCallback<EventCoreGeneratorProvider>() {
                @Override
                public EventCoreGeneratorProvider value() {
                    return new EventCoreGenerator(coreAppState);
                }
            });
        }

        @Override
        @GMainThread
        public void generateVisit(String sessionId, long timestamp) {
            mCoreAppState.getGIOMain().postEventToGMain(
                    new VisitEvent.EventBuilder(mCoreAppState)
                            .setSessionId(sessionId)
                            .setTimestamp(timestamp)
            );
        }

        @Override
        public void generateCustomEvent(String name, Map<String, String> variables) {
            mCoreAppState.getGIOMain().postEventToGMain(
                    new CustomEvent.EventBuilder(mCoreAppState)
                            .setEventName(name)
                            .setAttributes(variables)
            );
        }

        @Override
        public void generateConversionVariablesEvent(Map<String, String> variables) {
            mCoreAppState.getGIOMain().postEventToGMain(
                    new ConversionVariablesEvent.EventBuilder(mCoreAppState)
                            .setAttributes(variables)
            );
        }

        @Override
        public void generateLoginUserAttributesEvent(Map<String, String> attributes) {
            mCoreAppState.getGIOMain().postEventToGMain(
                    new LoginUserAttributesEvent.EventBuilder(mCoreAppState)
                            .setAttributes(attributes)
            );
        }

        @Override
        public void generateVisitorAttributesEvent(Map<String, String> attributes) {
            mCoreAppState.getGIOMain().postEventToGMain(
                    new VisitorAttributesEvent.EventBuilder(mCoreAppState)
                            .setAttributes(attributes)
            );
        }
    }
}
