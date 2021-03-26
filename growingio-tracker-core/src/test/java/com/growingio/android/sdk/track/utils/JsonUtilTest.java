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

package com.growingio.android.sdk.track.utils;

import com.google.common.truth.Truth;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
public class JsonUtilTest {

    @Test
    public void testJsonUtil() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        JSONObject fromMap = JsonUtil.copyFromMap(map);
        JSONObject fromString = JsonUtil.fromString("{\"key\":\"value\"}");
        Truth.assertThat(JsonUtil.equal(fromString, fromMap)).isTrue();

        JSONArray fromMapArray = new JSONArray();
        fromMapArray.put(fromMap);
        JSONArray fromStringArray = new JSONArray();
        fromStringArray.put(fromString);
        Truth.assertThat(JsonUtil.equal(fromMapArray, fromStringArray)).isTrue();

        Map<String, String> toMap = JsonUtil.copyToMap(fromMap);
        Truth.assertThat(toMap.containsKey("key")).isTrue();
        Truth.assertThat(toMap.containsValue("value")).isTrue();
    }
}
