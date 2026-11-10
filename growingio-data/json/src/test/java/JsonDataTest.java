/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
import com.google.common.truth.Truth;
import com.growingio.android.json.JsonDataFetcher;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class JsonDataTest {
    @Test
    public void dataFormat() {
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("jsonTest")
                .build();

        EventFormatData eventData = EventFormatData.format(customEvent);
        DataFetcher<EventByteArray> dataFetcher = new JsonDataFetcher(eventData);
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
        ArrayList<byte[]> arrayList = new ArrayList<>();
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("merge")
                .build();
        CustomEvent customEvent2 = new CustomEvent.Builder()
                .setEventName("cpacm")
                .build();

        arrayList.add(EventBuilderProvider.toJson(customEvent).toString().getBytes());
        arrayList.add(EventBuilderProvider.toJson(customEvent2).toString().getBytes());

        EventFormatData eventData = EventFormatData.merge(arrayList);
        DataFetcher<EventByteArray> dataFetcher = new JsonDataFetcher(eventData);
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

    @Test
    public void dataMergeReplacesOnlyPendingEvents() throws JSONException {
        ArrayList<byte[]> arrayList = new ArrayList<>();
        arrayList.add(cachedJson("legacy", "generated-device"));
        byte[] pendingData = cachedJson("pending", "generated-device");
        arrayList.add(pendingData);

        EventFormatData eventData = EventFormatData.merge(arrayList, Arrays.asList(false, true), "visitor-001");
        EventByteArray data = new JsonDataFetcher(eventData).executeData();

        JSONArray jsonArray = new JSONArray(new String(data.getBodyData()));
        Truth.assertThat(jsonArray.length()).isEqualTo(2);
        Truth.assertThat(jsonArray.getJSONObject(0).getString("deviceId")).isEqualTo("generated-device");
        Truth.assertThat(jsonArray.getJSONObject(1).getString("deviceId")).isEqualTo("visitor-001");
        Truth.assertThat(jsonArray.getJSONObject(1).getString("eventName")).isEqualTo("pending");
        // 数据库原文不变
        Truth.assertThat(new JSONObject(new String(pendingData)).getString("deviceId")).isEqualTo("generated-device");
    }

    @Test
    public void dataMergeWithoutReplacementKeepsPendingEvents() throws JSONException {
        ArrayList<byte[]> arrayList = new ArrayList<>();
        arrayList.add(cachedJson("pending", "generated-device"));

        EventFormatData eventData = EventFormatData.merge(arrayList, Arrays.asList(true), "");
        EventByteArray data = new JsonDataFetcher(eventData).executeData();

        JSONArray jsonArray = new JSONArray(new String(data.getBodyData()));
        Truth.assertThat(jsonArray.getJSONObject(0).getString("deviceId")).isEqualTo("generated-device");
    }

    @Test
    public void malformedPendingEventDoesNotBlockBatch() throws JSONException {
        ArrayList<byte[]> arrayList = new ArrayList<>();
        arrayList.add("{broken}".getBytes());
        arrayList.add(cachedJson("pending", "generated-device"));

        EventFormatData eventData = EventFormatData.merge(arrayList, Arrays.asList(true, true), "visitor-001");
        EventByteArray data = new JsonDataFetcher(eventData).executeData();

        // 替换失败的事件按原数据拼接，其余事件照常替换，整批不失败
        String body = new String(data.getBodyData());
        Truth.assertThat(body).startsWith("[{broken},");
        JSONObject pending = new JSONObject(body.substring("[{broken},".length(), body.length() - 1));
        Truth.assertThat(pending.getString("deviceId")).isEqualTo("visitor-001");
    }

    private byte[] cachedJson(String eventName, String deviceId) throws JSONException {
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName(eventName)
                .build();
        JSONObject cachedJson = EventBuilderProvider.toJson(customEvent);
        cachedJson.put("deviceId", deviceId);
        return cachedJson.toString().getBytes();
    }

}
