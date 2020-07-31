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

package com.growingio.android.sdk.autotrack.variation;

import com.growingio.android.sdk.autotrack.events.PageAttributesEvent;
import com.growingio.android.sdk.autotrack.events.PageEvent;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.variation.MarshallerConstants;
import com.growingio.android.sdk.track.variation.TrackEventJsonMarshaller;

import org.json.JSONException;
import org.json.JSONObject;

public class AutotrackEventJsonMarshaller extends TrackEventJsonMarshaller {
    @Override
    public JSONObject marshall(GEvent event) {
        JSONObject json = super.marshall(event);
        if (json == null) {
            return null;
        }
        try {
            if (event instanceof PageEvent) {
                writePageEvent(json, (PageEvent) event);
            } else if (event instanceof PageAttributesEvent) {
                writePageAttributesEvent(json, (PageAttributesEvent) event);
            }

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writePageEvent(JSONObject generator, PageEvent event) throws JSONException {
        generator.put(MarshallerConstants.Key.PAGE_NAME, event.getPageName());
        generator.put(MarshallerConstants.Key.ORIENTATION, event.getOrientation());
        generator.put(MarshallerConstants.Key.TITLE, event.getTitle());
        generator.put(MarshallerConstants.Key.NETWORK_STATE, event.getNetworkState());
        generator.put(MarshallerConstants.Key.REFERRAL_PAGE, event.getReferralPage());
    }

    private void writePageAttributesEvent(JSONObject generator, PageAttributesEvent event) throws JSONException {
        generator.put(MarshallerConstants.Key.PAGE_NAME, event.getPageName());
        generator.put(MarshallerConstants.Key.PAGE_SHOW_TIMESTAMP, event.getPageShowTimestamp());
    }
}
