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

package com.growingio.android.sdk.autotrack.events;

import androidx.annotation.StringDef;

import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PageEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
    public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ORIENTATION_PORTRAIT, ORIENTATION_LANDSCAPE})
    public @interface Orientation {
    }

    private final String mPageName;
    private final String mOrientation;
    private final String mTitle;
    private final String mReferralPage;

    protected PageEvent(Builder eventBuilder) {
        super(eventBuilder);
        mPageName = eventBuilder.mPageName;
        mOrientation = eventBuilder.mOrientation;
        mTitle = eventBuilder.mTitle;
        mReferralPage = eventBuilder.mReferralPage;
    }

    public String getPageName() {
        return mPageName;
    }

    public String getOrientation() {
        return mOrientation;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getReferralPage() {
        return mReferralPage;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("pageName", mPageName);
            json.put("orientation", mOrientation);
            json.put("title", mTitle);
            json.put("referralPage", mReferralPage);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends BaseBuilder<PageEvent> {
        private String mPageName;
        private String mOrientation = ORIENTATION_PORTRAIT;
        private String mTitle;
        private String mReferralPage = "";

        public Builder() {
            super();
        }

        public Builder setPageName(String pageName) {
            mPageName = pageName;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setReferralPage(String referralPage) {
            mReferralPage = referralPage;
            return this;
        }

        public Builder setOrientation(@Orientation String orientation) {
            mOrientation = orientation;
            return this;
        }

        @Override
        public String getEventType() {
            return AutotrackEventType.PAGE;
        }

        @Override
        public PageEvent build() {
            return new PageEvent(this);
        }

        public Builder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }
    }
}
