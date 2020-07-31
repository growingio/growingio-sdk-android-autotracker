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

import android.util.Log;

import com.growingio.android.sdk.track.ErrorLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ArgumentChecker {
    private static final String TAG = "GIO.ArgumentChecker";
    private static final int MAX_VALUE_SIZE = 1000;

    private ArgumentChecker() {
    }

    public static boolean isIllegalEventName(String eventName) {
        return isIllegalEventName(eventName, true);
    }

    public static boolean isIllegalEventName(String eventName, boolean showLog) {
        if (eventName == null || eventName.trim().length() == 0 || eventName.length() > 50) {
            if (showLog) {
                Log.e(TAG, ErrorLog.EVENT_NAME_ILLEGAL);
            }
            return true;
        }
        return false;
    }

    public static boolean isIllegalValue(String value, boolean showLog) {
        boolean result = value == null
                || value.trim().length() == 0
                || value.length() > MAX_VALUE_SIZE;
        if (result && showLog) {
            Log.e(TAG, ErrorLog.VALUE_BE_EMPTY);
        }
        return result;
    }


    public static boolean isIllegalValue(String value) {
        return isIllegalValue(value, true);
    }

    public static boolean isIllegalValue(Number number) {
        if (number == null) {
            Log.e(TAG, ErrorLog.VALUE_BE_EMPTY);
            return true;
        }
        return false;
    }


    public static JSONObject validJSONObject(JSONObject jsonObject) {
        return validJSONObject(jsonObject, true);
    }

    /**
     * SDK通用对外JSONObject参数校验:
     * - 有效键值对最多
     *
     * @param allowValueNull 允许JSONObject中的value为empty， 允许JSONObject为null或者empty
     * @return null --> 没有任何合法参数, JSONObject copy后只包含有效键值对的JSONObject
     */
    public static JSONObject validJSONObject(JSONObject jsonObject, boolean allowValueNull) {
        if (allowValueNull) {
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
        } else {
            if (jsonObject == null || jsonObject.length() == 0) {
                Log.e("GrowingIO", ErrorLog.VALUE_BE_EMPTY);
                return null;
            }
        }
        if (jsonObject.length() > 100) {
            Log.e("GrowingIO", ErrorLog.JSON_TOO_LONG);
            return null;
        }
        JSONObject copy = new JSONObject();
        int validLength = 0;
        try {
            for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext();) {
                String key = iterator.next();
                Object value = jsonObject.opt(key);
                if (isIllegalEventName(key, true)) {
                    return null;
                }
                if ((value instanceof JSONObject || value instanceof JSONArray)) {
                    Log.e("GrowingIO", ErrorLog.jsonObjArrayNotSupport(key));
                    return null;
                }
                if (value instanceof String) {
                    if (allowValueNull && ((String) value).trim().length() == 0) {
                        value = "";
                    } else if (isIllegalValue((String) value, true)) {
                        return null;
                    }
                }
                validLength += 1;
                copy.put(key, value);
                if (validLength >= 100) {
                    break;
                }
            }
        } catch (JSONException e) {
            // ignore
        }
        int invalidNum = jsonObject.length() - validLength;
        if (validLength == 0 && !allowValueNull) {
            Log.e("GrowingIO", ErrorLog.JSON_VALUE_EMPTY_VALID);
            copy = null;
        } else if (invalidNum > 0) {
            Log.e("GrowingIO", ErrorLog.JSON_KEY_VALUE_NOT_VALID);
            copy = null;
        }
        return copy;
    }

}
