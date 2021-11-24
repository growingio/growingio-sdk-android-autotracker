/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.track.events.cdp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ResourceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String mKey;
    private final String mId;

    public ResourceItem(String key, String id) {
        mKey = key;
        mId = id;
    }

    public String getKey() {
        return mKey == null ? "" : mKey;
    }

    public String getId() {
        return mId == null ? "" : mId;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", mKey);
            jsonObject.put("id", mId);
        } catch (JSONException ignored) {
        }
        return jsonObject;
    }
}
