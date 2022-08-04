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

package com.growingio.android.advert;

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 *
 * @author cpacm 2022/8/3
 */
public class ActivateEvent extends BaseEvent {

    private static final String TYPE_NAME = "activate";

    private final String oaid;
    private final String googleId;
    private final String linkId;
    private final String clickId;
    private final String clickTm;
    private final String classification; // cl 类别

    protected ActivateEvent(Builder eventBuilder) {
        super(eventBuilder);
        oaid = eventBuilder.oaid;
        googleId = eventBuilder.googleId;
        linkId = eventBuilder.linkId;
        clickId = eventBuilder.clickId;
        clickTm = eventBuilder.clickTm;
        classification = eventBuilder.classification;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {

            if (!TextUtils.isEmpty(googleId)) {
                json.put("gaid", googleId);
            }
            if (!TextUtils.isEmpty(oaid)) {
                json.put("oaid", oaid);
            }
            if (!TextUtils.isEmpty(linkId)) {
                json.put("link_id", linkId);
            }
            if (!TextUtils.isEmpty(clickId)) {
                json.put("click_id", clickId);
            }
            if (!TextUtils.isEmpty(clickTm)) {
                json.put("tm_click", clickTm);
            }
            json.put("cl", checkValueSafe(classification));
        } catch (JSONException ignored) {
        }
        return json;
    }

    public String getOaid() {
        return oaid;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getLinkId() {
        return linkId;
    }

    public String getClickId() {
        return clickId;
    }

    public String getClickTm() {
        return clickTm;
    }

    public String getClassification() {
        return classification;
    }

    public static final class Builder extends BaseBuilder<ActivateEvent> {
        private String oaid;
        private String googleId;
        private String linkId;
        private String clickId;
        private String clickTm;
        private String classification; // cl 类别

        public Builder() {
            super();
        }

        public Builder setAdvertData(AdvertData ad) {
            if(ad==null) return this;
            linkId = ad.linkID;
            clickId = ad.clickID;
            clickTm = ad.clickTM;
            classification = "defer";
            return this;
        }

        @Override
        public String getEventType() {
            return TYPE_NAME;
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            oaid = deviceInfo.getOaid();
            googleId = deviceInfo.getGoogleAdId();
        }

        @Override
        public ActivateEvent build() {
            return new ActivateEvent(this);
        }
    }
}
