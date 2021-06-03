package com.growingio.android.okhttp3;

import android.content.Context;
import android.util.Log;

import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
public class Okhttp3Test {

    @Before
    public void prepare() {
        mockStatic(Log.class);
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
                .addParam("stm", String.valueOf(time));
    }

    @Test
    public void sendTest() {
        OkhttpLibraryGioModule module = new OkhttpLibraryGioModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(fakeContext, trackerRegistry);

        ModelLoader<EventUrl, EventResponse> modelLoader = trackerRegistry.getModelLoader(EventUrl.class, EventResponse.class);
        EventUrl eventUrl = initEventUrl("http://106.75.81.105:8080");
        EventResponse response = modelLoader.buildLoadData(eventUrl).fetcher.executeData();
        assertThat(response.isSucceeded()).isTrue();
        eventUrl = initEventUrl("http://localhost/");
        response = modelLoader.buildLoadData(eventUrl).fetcher.executeData();
        assertThat(response.isSucceeded()).isFalse();

    }
}
