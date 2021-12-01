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
package com.growingio.android.okhttp3;

import android.content.Context;
import android.util.Log;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.middleware.http.HttpDataFetcher;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * <p>
 *
 * @author cpacm 2021/6/3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
@PowerMockIgnore("javax.net.ssl.*")
public class Okhttp3Test extends MockServer {

    @Before
    public void prepare() throws IOException {
        mockStatic(Log.class);
        mockEventsApiServer();
        start();
    }

    public void mockEventsApiServer() {
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String url = request.getRequestUrl().toString();
                URI uri = URI.create(url);
                checkPath(uri.getPath());


                String json = request.getBody().readUtf8();
                dispatchReceivedEvents(json);
                return new MockResponse().setResponseCode(200);
            }
        };
        setDispatcher(dispatcher);
    }

    @Mock
    Context fakeContext;

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

    private void checkPath(String path) {
        String expectedPath = "/v3/projects/bfc5d6a3693a110d/collect";
        Truth.assertThat(path).isEqualTo(expectedPath);
    }

    private void dispatchReceivedEvents(String json) {
        Truth.assertThat(json).isEqualTo("cpacm");
    }

    @Test
    public void sendTest() {
        OkhttpLibraryGioModule module = new OkhttpLibraryGioModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(fakeContext, trackerRegistry);

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
    }
}
