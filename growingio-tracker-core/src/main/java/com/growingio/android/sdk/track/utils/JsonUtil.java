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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static Map<String, String> copyToMap(JSONObject jsonObject) {
        if (jsonObject == null)
            return null;
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                if (jsonObject.get(key) == JSONObject.NULL) {
                    map.put(key, null);
                } else {
                    map.put(key, String.valueOf(jsonObject.get(key)));
                }
            } catch (JSONException e) {
                // ignore
            }
        }
        return map;
    }

}
