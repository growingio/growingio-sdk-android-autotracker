/*
 *
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import android.app.Application;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.json.JsonLibraryModule;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.http.EventData;
import com.growingio.android.sdk.track.http.EventStream;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class JsonDataTest {
    private Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }


    @Test
    public void dataTransfer() {
        JsonLibraryModule module = new JsonLibraryModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(application, trackerRegistry);

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("jsonTest")
                .build();

        EventData eventData = new EventData(Collections.singletonList(customEvent));
        trackerRegistry.getModelLoader(EventData.class, EventStream.class)
                .buildLoadData(eventData).fetcher
                .loadData(new DataFetcher.DataCallback<EventStream>() {
                    @Override
                    public void onDataReady(EventStream data) {
                        try {
                            JSONArray jsonArray = new JSONArray(new String(data.getBodyData()));
                            String eventName = jsonArray.getJSONObject(0).optString("eventName");
                            String eventType = jsonArray.getJSONObject(0).optString("eventType");
                            Truth.assertThat(eventType).isEqualTo("CUSTOM");
                            Truth.assertThat(eventName).isEqualTo("jsonTest");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Truth.assertThat(true).isFalse();
                        }
                    }

                    @Override
                    public void onLoadFailed(Exception e) {
                        Truth.assertThat(true).isFalse();
                    }
                });
    }

}
