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

package com.growingio.android.sdk.track;

/**
 * 显示给用户的错误提示
 */
public class ErrorLog {

    public static final String EVENT_NAME_ILLEGAL = "当前数据的标识符不合法。合法的标识符的详细定义请参考：https://docs.growingio.com/docs/developer-manual/sdkintegrated/android-sdk/android-sdk-api/customize-api";
    public static final String VALUE_BE_EMPTY = "当前事件类型数据的值不合法。值不能为空或者null";
    public static final String JSON_VALUE_EMPTY_VALID = "当前JSONObject没有任何有效数据, 发送失败";
    public static final String JSON_TOO_LONG = "JSONObject传参最长仅支持100个键值对";
    public static final String JSON_KEY_VALUE_NOT_VALID = "JSONObject传参中键值对不合法, 发送失败";
    public static final String TRACK_FRAGMENT_ERROR = "trackAllFragment策略与全局策略相同， 不需要单独设置";
    public static final String USER_ID_TOO_LONG = "GrowingIO.setUserId(VALUE):VALUE长度大于1000，不发送";

    private ErrorLog() {
    }

//    public static String jsonValueTooLong(String key) {
//        return "JSON Object中有键值对，值的字符个数在1000个以下, 发送失败, key: " + key;
//    }
//
//    public static String jsonObjArrayNotSupport(String key) {
//        return "jsonObject 不支持嵌套JSONObject或者JsonArray， 发送失败, key:" + key;
//    }
//
//    public static String jsonNullValue(String key) {
//        return "value 不能为null, 可以传字符串\"null\"代替: " + key;
//    }
//
//    public static String jsonInValidKey(String key) {
//        return "JSONObject中的key要求: 1. 非空, 2. 长度小于50: " + key;
//    }
//
//    public static String argumentBeNull(String name) {
//        return "参数:" + name + "不能为null";
//    }
}
