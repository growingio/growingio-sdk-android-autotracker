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

package com.growingio.autotest.autotracker.impression;

import com.growingio.android.sdk.autotrack.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.utils.JsonUtil;
import com.growingio.android.sdk.track.utils.ObjectUtils;
import com.growingio.autotest.help.MockEventsApiServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

final class OnReceivedViewImpressionEventsListener extends MockEventsApiServer.OnReceivedEventListener {
    private final Map<String, Long> mReceivedPages = new HashMap<>();
    private final List<PageLevelCustomEvent> mExpectReceivedImpressions;
    private final AtomicBoolean mReceivedEvent;

    OnReceivedViewImpressionEventsListener(AtomicBoolean receivedEvents, PageLevelCustomEvent... expectImpressionEvents) {
        mReceivedEvent = receivedEvents;
        mReceivedEvent.set(false);
        mExpectReceivedImpressions = new ArrayList<>(Arrays.asList(expectImpressionEvents));
    }

    @Override
    protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String path = jsonObject.getString("path");
            mReceivedPages.put(path, jsonObject.getLong("timestamp"));
        }
    }

    @Override
    protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            for (int j = 0; j < mExpectReceivedImpressions.size(); j++) {
                String path = jsonObject.getString("path");
                Map<String, String> attributes = JsonUtil.copyToMap(jsonObject.optJSONObject("attributes"));
                PageLevelCustomEvent customEvent = mExpectReceivedImpressions.get(j);
                if (path.equals(customEvent.getPath())
                        && jsonObject.getString("eventName").equals(customEvent.getEventName())
                        && ObjectUtils.equals(attributes, customEvent.getAttributes())
                        && jsonObject.getLong("pageShowTimestamp") == mReceivedPages.get(path)) {
                    mExpectReceivedImpressions.remove(j);
                    break;
                }
            }
            if (mExpectReceivedImpressions.isEmpty()) {
                mReceivedEvent.set(true);
            }
        }
    }
}
