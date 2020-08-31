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

package com.growingio.android.sdk.track.webservices.message;

import org.json.JSONException;
import org.json.JSONObject;

public class QuitMessage {
    private static final String TAG = "QuitMessage";

    public static final String MSG_TYPE = "quit";
    private final String mMsgType;

    public QuitMessage() {
        mMsgType = MSG_TYPE;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", mMsgType);
        } catch (JSONException ignored) {
        }
        return json;
    }
}
