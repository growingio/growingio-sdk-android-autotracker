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

import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * 唤醒事件
 *
 * @author cpacm 2022/8/3
 */
public class ReengageEvent extends BaseEvent {

    private static final String TYPE_NAME = TrackEventType.REENGAGE;

    private final String linkId;
    private final String clickId;
    private final String clickTm;
    private final String varargs; // 事件参数

    protected ReengageEvent(Builder eventBuilder) {
        super(eventBuilder);
        linkId = eventBuilder.linkId;
        clickId = eventBuilder.clickId;
        clickTm = eventBuilder.clickTm;
        varargs = eventBuilder.varargs;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {

            if (!TextUtils.isEmpty(linkId)) {
                json.put("link_id", linkId);
            }
            if (!TextUtils.isEmpty(clickId)) {
                json.put("click_id", clickId);
            }
            if (!TextUtils.isEmpty(clickTm)) {
                json.put("tm_click", clickTm);
            }
            json.put("var", checkValueSafe(varargs));
        } catch (JSONException ignored) {
        }
        return json;
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

    public String getVarargs() {
        return varargs;
    }

    public static final class Builder extends BaseBuilder<ReengageEvent> {
        private String linkId;
        private String clickId;
        private String clickTm;
        private String varargs;

        public Builder() {
            super();
        }

        public Builder setAdvertData(AdvertData ad) {
            if (ad == null) return this;
            linkId = ad.linkID;
            clickId = ad.clickID;
            clickTm = ad.clickTM;
            varargs = ad.customParams;
            return this;
        }

        @Override
        public String getEventType() {
            return TYPE_NAME;
        }

        @Override
        public ReengageEvent build() {
            return new ReengageEvent(this);
        }
    }
}
