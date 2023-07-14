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

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.json.JsonLibraryModule;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.EventStateProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class JsonDataTest {
    private Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }

    @Test
    public void dataFormat() {
        JsonLibraryModule module = new JsonLibraryModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(application, trackerRegistry);

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("jsonTest")
                .build();

        EventFormatData eventData = EventFormatData.format(customEvent);
        DataFetcher<EventByteArray> dataFetcher =
                trackerRegistry.getModelLoader(EventFormatData.class, EventByteArray.class)
                        .buildLoadData(eventData).fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventByteArray.class);
        EventByteArray data = dataFetcher.executeData();
        try {
            JSONObject jsonObject = new JSONObject(new String(data.getBodyData()));
            String eventName = jsonObject.optString("eventName");
            String eventType = jsonObject.optString("eventType");
            Truth.assertThat(eventType).isEqualTo("CUSTOM");
            Truth.assertThat(eventName).isEqualTo("jsonTest");
        } catch (JSONException e) {
            e.printStackTrace();
            Truth.assertThat(true).isFalse();
        }
    }

    @Test
    public void dataMerge() {
        JsonLibraryModule module = new JsonLibraryModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(application, trackerRegistry);

        ArrayList<byte[]> arrayList = new ArrayList<>();
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("merge")
                .build();
        CustomEvent customEvent2 = new CustomEvent.Builder()
                .setEventName("cpacm")
                .build();

        arrayList.add(EventStateProvider.get().toJson(customEvent).toString().getBytes());
        arrayList.add(EventStateProvider.get().toJson(customEvent2).toString().getBytes());

        EventFormatData eventData = EventFormatData.merge(arrayList);
        DataFetcher<EventByteArray> dataFetcher = trackerRegistry.getModelLoader(EventFormatData.class, EventByteArray.class)
                .buildLoadData(eventData).fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventByteArray.class);
        EventByteArray data = dataFetcher.executeData();

        try {
            JSONArray jsonArray = new JSONArray(new String(data.getBodyData()));
            Truth.assertThat(jsonArray.length()).isEqualTo(2);
            JSONObject json1 = jsonArray.getJSONObject(0);
            JSONObject json2 = jsonArray.getJSONObject(1);
            Truth.assertThat(json1.opt("eventName")).isEqualTo("merge");
            Truth.assertThat(json2.opt("eventName")).isEqualTo("cpacm");
        } catch (JSONException e) {
            e.printStackTrace();
            Truth.assertThat(true).isFalse();
        }

    }

}
