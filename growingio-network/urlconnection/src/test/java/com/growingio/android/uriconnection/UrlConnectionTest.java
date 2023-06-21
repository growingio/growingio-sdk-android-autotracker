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

package com.growingio.android.uriconnection;


import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.middleware.http.HttpDataFetcher;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.urlconnection.HttpException;
import com.growingio.android.urlconnection.LogTime;
import com.growingio.android.urlconnection.UrlConnectionGioModule;

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
public class UrlConnectionTest {

    private MockWebServer mockWebServer;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String url = request.getRequestUrl().toString();
                URI uri = URI.create(url);
                if (uri.getPath().contains("errorCode")) {
                    return new MockResponse().setResponseCode(503);
                } else if (uri.getPath().contains("redirect")) {
                    return new MockResponse().setResponseCode(304).setHeader("Location", "http://localhost:8910/404");
                } else if (uri.getPath().contains("404")) {
                    return new MockResponse().setResponseCode(-1);
                }
                checkPath(uri.getPath());
                return new MockResponse().setResponseCode(200);
            }
        });
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
    public void sendTest() throws IOException {
        mockWebServer.start(8910);
        UrlConnectionGioModule module = new UrlConnectionGioModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(application, trackerRegistry);

        ModelLoader<EventUrl, EventResponse> modelLoader = trackerRegistry.getModelLoader(EventUrl.class, EventResponse.class);
        EventUrl eventUrl = initEventUrl("http://localhost:8910/");
        EventResponse response = modelLoader.buildLoadData(eventUrl).fetcher.executeData();
        assertThat(response.isSucceeded()).isTrue();
        Truth.assertThat(response.getStream()).isInstanceOf(InputStream.class);
        Truth.assertThat(response.getUsedBytes()).isEqualTo(0L);

        HttpDataFetcher<EventResponse> dataFetcher = (HttpDataFetcher<EventResponse>) modelLoader.buildLoadData(eventUrl).fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventResponse.class);
        dataFetcher.loadData(new LoadDataFetcher.DataCallback<EventResponse>() {
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

        EventUrl eventUrl2 = initEventUrl("http://localhost:8010/");
        HttpDataFetcher<EventResponse> dataFetcher2 = (HttpDataFetcher<EventResponse>) modelLoader.buildLoadData(eventUrl2).fetcher;
        dataFetcher2.loadData(new LoadDataFetcher.DataCallback<EventResponse>() {
            @Override
            public void onDataReady(EventResponse response) {

            }

            @Override
            public void onLoadFailed(Exception e) {
                Truth.assertThat(e).isInstanceOf(HttpException.class);
            }
        });

        EventUrl eventUrl3 = new EventUrl("http://localhost:8910/", System.currentTimeMillis()).addPath("errorCode");
        HttpDataFetcher<EventResponse> dataFetcher3 = (HttpDataFetcher<EventResponse>) modelLoader.buildLoadData(eventUrl3).fetcher;
        dataFetcher3.loadData(new LoadDataFetcher.DataCallback<EventResponse>() {
            @Override
            public void onDataReady(EventResponse response) {

            }

            @Override
            public void onLoadFailed(Exception e) {
                System.out.println(e.toString());
                Truth.assertThat(e).isInstanceOf(HttpException.class);
            }
        });

        EventUrl eventUrl4 = new EventUrl("http://localhost:8910/", System.currentTimeMillis())
                .addPath("redirect");
        HttpDataFetcher<EventResponse> dataFetcher4 = (HttpDataFetcher<EventResponse>) modelLoader.buildLoadData(eventUrl4).fetcher;
        dataFetcher4.loadData(new LoadDataFetcher.DataCallback<EventResponse>() {
            @Override
            public void onDataReady(EventResponse response) {

            }

            @Override
            public void onLoadFailed(Exception e) {
                Truth.assertThat(e).isInstanceOf(HttpException.class);
            }
        });
    }

    @Test
    public void logTimeTest() {
        long time = LogTime.getLogTime() - 1000L;
        double time2 = LogTime.getElapsedMillis(time);
        Truth.assertThat(time).isAtLeast(100000L);
        Truth.assertThat(time2).isGreaterThan(0);
    }

    @Test
    public void httpException() throws IOException {
        HttpException httpException2 = new HttpException(404);
        Truth.assertThat(httpException2.getStatusCode()).isEqualTo(404);
    }


}
