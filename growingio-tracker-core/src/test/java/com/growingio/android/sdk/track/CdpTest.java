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

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.cdp.ResourceItem;
import com.growingio.android.sdk.track.events.cdp.ResourceItemCustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CdpTest {

    Application application = ApplicationProvider.getApplicationContext();
    private TrackMainThread trackMainThread;

    @Before
    public void setup() {
        TrackerContext.init(application);
        Tracker tracker = new Tracker(application);
        tracker.setLoginUserId("cpacm");
        trackMainThread = new TrackMainThread(new CoreConfiguration("CdpTest", "growingio://cdp"));
        trackMainThread.addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                eventBuilder.addExtraParam("dataSourceId", "12345");
            }

            @Override
            public void eventDidBuild(GEvent event) {

            }
        });
    }

    @Test
    public void cdpTest() {
        EventBuildInterceptor testInterceptor = new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {

            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof ResourceItemCustomEvent) {
                    ResourceItemCustomEvent rEvent = (ResourceItemCustomEvent) event;
                    JSONObject jsonObject = rEvent.toJSONObject();
                    Truth.assertThat(jsonObject.opt("dataSourceId")).isEqualTo("12345");
                    //Truth.assertThat(jsonObject.opt("gioId")).isEqualTo("cpacm");
                    Truth.assertThat(jsonObject.opt("userId")).isEqualTo("cpacm");
                    Truth.assertThat(jsonObject.opt("eventName")).isEqualTo("custom");
                    try {
                        JSONObject item = jsonObject.getJSONObject("resourceItem");
                        Truth.assertThat(item.opt("id")).isEqualTo("itemId");
                        Truth.assertThat(item.opt("key")).isEqualTo("itemKey");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        trackMainThread.addEventBuildInterceptor(testInterceptor);

        Map<String, String> attrs = new HashMap<>();
        attrs.put("name", "cpacm");
        trackMainThread.onGenerateGEvent(
                new ResourceItemCustomEvent.Builder()
                        .setEventName("custom")
                        .setAttributes(attrs)
                        .setResourceItem(new ResourceItem("itemKey", "itemId"))
        );
        trackMainThread.removeEventBuildInterceptor(testInterceptor);
    }

}
