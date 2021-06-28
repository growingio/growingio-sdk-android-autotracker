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

package com.growingio.android.volley;


import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class VolleyTest {

    private MockWebServer mockWebServer;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String url = request.getRequestUrl().toString();
                URI uri = URI.create(url);
                checkPath(uri.getPath());
                return new MockResponse().setResponseCode(200);
            }
        });
        mockWebServer.start(8910);
    }

    private void checkPath(String path) {
        String expectedPath = "/v3/projects/bfc5d6a3693a110d/collect";
        Truth.assertThat(path).isEqualTo(expectedPath);
    }

    private EventUrl initEventUrl(String host) {
        long time = System.currentTimeMillis();
        return new EventUrl(host, time)
                .addPath("v3")
                .addPath("projects")
                .addPath("bfc5d6a3693a110d")
                .addPath("collect")
                .addHeader("name", "cpacm")
                .setBodyData("cpacm".getBytes())
                .addParam("stm", String.valueOf(time));
    }

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void sendTest() {
        VolleyLibraryGioModule module = new VolleyLibraryGioModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(application, trackerRegistry);

        ModelLoader<EventUrl, EventResponse> modelLoader = trackerRegistry.getModelLoader(EventUrl.class, EventResponse.class);
        EventUrl eventUrl = initEventUrl("http://localhost:8910/");
        EventResponse response = modelLoader.buildLoadData(eventUrl).fetcher.executeData();
        assertThat(response.isSucceeded()).isTrue();
        Truth.assertThat(response.getStream()).isInstanceOf(InputStream.class);
        Truth.assertThat(response.getUsedBytes()).isEqualTo(0L);

        DataFetcher<EventResponse> dataFetcher = modelLoader.buildLoadData(eventUrl).fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventResponse.class);
        dataFetcher.loadData(new DataFetcher.DataCallback<EventResponse>() {
            @Override
            public void onDataReady(EventResponse response) {
                assertThat(response.isSucceeded()).isTrue();
                Truth.assertThat(response.getStream()).isInstanceOf(InputStream.class);
                Truth.assertThat(response.getUsedBytes()).isEqualTo(0L);
                dataFetcher.cleanup();
                dataFetcher.cancel();
            }

            @Override
            public void onLoadFailed(Exception e) {

            }
        });
    }


}
