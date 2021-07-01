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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    /**
     * @return true if left and right key value equal,
     */
    public static boolean equals(JSONObject left, JSONObject right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (left.length() != right.length()) {
            return false;
        }
        try {
            for (Iterator<String> iterator = left.keys(); iterator.hasNext();) {
                String key = iterator.next();
                if (!right.has(key))
                    return false;
                Object leftValue = left.get(key);
                Object rightValue = right.get(key);
                // leftValue and rightValue all not null
                if (!jsonEquals(leftValue, rightValue)) {
                    return false;
                }
            }
        } catch (JSONException e) {
            // ignore
        }
        return true;
    }

    private static boolean jsonEquals(Object left, Object right) {
        if (ObjectUtils.equals(left, right)) {
            return true;
        } else if (left instanceof JSONObject && right instanceof JSONObject) {
            return equals((JSONObject) left, (JSONObject) right);
        } else if (left instanceof JSONArray && right instanceof JSONArray) {
            return equals((JSONArray) left, (JSONArray) right);
        }
        return false;
    }

    /**
     * @return true if left and right key value equal,
     */
    public static boolean equals(JSONArray left, JSONArray right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (left.length() != right.length()) {
            return false;
        }
        try {
            for (int i = 0; i < left.length(); i++) {
                Object leftValue = left.get(i);
                Object rightValue = right.get(i);
                if (!jsonEquals(leftValue, rightValue)) {
                    return false;
                }
            }
        } catch (JSONException e) {
            // ignore
        }
        return true;
    }

    public static JSONObject fromString(String json) {
        if (json == null) return null;
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }
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

    public static JSONObject copyFromMap(Map<String, String> map) {
        if (map == null)
            return null;
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}
