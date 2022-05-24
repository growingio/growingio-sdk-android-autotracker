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

package com.growingio.android.sdk.track.middleware;


import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.http.EventEncoder;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class HttpSenderTest {
    private final Application application = ApplicationProvider.getApplicationContext();
    private EventHttpSender eventHttpSender;

    @Before
    public void setup() {
        TrackerContext.init(application);
        eventHttpSender = new EventHttpSender();
    }

    @Test
    public void eventUrlTest() {
        EventUrl eventUrl = new EventUrl("https://localhost", 10000L)
                .addPath("v3")
                .addPath("projects")
                .addPath("test")
                .addPath("collect")
                .addParam("stm", String.valueOf(10000L))
                .addHeader("referer", "okhttp")
                .setBodyData("cpacm".getBytes());
        Truth.assertThat(eventUrl.toUrl()).isEqualTo("https://localhost/v3/projects/test/collect?stm=10000");
        Truth.assertThat(eventUrl.toString()).isEqualTo("https://localhost/v3/projects/test/collect?stm=10000");
    }

    @Test
    public void sendTest() throws IOException {
        Truth.assertThat(eventHttpSender.send(null, null).isSucceeded()).isFalse();
        GEvent gEvent = new CustomEvent.Builder().build();
        Truth.assertThat(eventHttpSender.send(Serializer.objectSerialize(gEvent), "").isSucceeded()).isFalse();
        TrackerContext.get().getRegistry()
                .register(EventUrl.class, EventResponse.class, new TestModelFactory<>(new EventResponse(true)));

        EventUrl eventUrl = new EventUrl("https://localhost", 10000L)
                .addPath("v3")
                .addPath("projects")
                .addPath("test")
                .addPath("collect")
                .addParam("stm", String.valueOf(10000L))
                .setBodyData("cpacm".getBytes());
        TrackerContext.get().getRegistry()
                .register(EventEncoder.class, EventEncoder.class, new TestModelFactory<>(new EventEncoder(eventUrl)));
        SendResponse response = eventHttpSender.send(Serializer.objectSerialize(gEvent), "");
        Truth.assertThat(response.getUsedBytes()).isEqualTo("cpacm".getBytes().length);

    }

}
