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

package com.growingio.android.sdk.track.events;

import androidx.annotation.StringDef;

import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public class PageEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
    public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ORIENTATION_PORTRAIT, ORIENTATION_LANDSCAPE})
    public @interface Orientation {
    }

    private final String mPath;
    private final String mOrientation;
    private final String mTitle;
    private final String mReferralPage;

    protected PageEvent(Builder eventBuilder) {
        super(eventBuilder);
        mPath = eventBuilder.mPath;
        mOrientation = eventBuilder.mOrientation;
        mTitle = eventBuilder.mTitle;
        mReferralPage = eventBuilder.mReferralPage;
    }

    public String getPath() {
        return checkValueSafe(mPath);
    }

    public String getOrientation() {
        return checkValueSafe(mOrientation);
    }

    public String getTitle() {
        return checkValueSafe(mTitle);
    }

    public String getReferralPage() {
        return checkValueSafe(mReferralPage);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("path", getPath());
            json.put("orientation", getOrientation());
            json.put("title", getTitle());
            json.put("referralPage", getReferralPage());
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends BaseAttributesEvent.Builder<PageEvent> {
        private String mPath;
        private String mOrientation = ORIENTATION_PORTRAIT;
        private String mTitle;
        private String mReferralPage = "";

        public Builder() {
            super(AutotrackEventType.PAGE);
        }

        public Builder setPath(String path) {
            mPath = path;
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

        public String getPath() {
            return mPath;
        }

        @Override
        public PageEvent build() {
            return new PageEvent(this);
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
        }

        public Builder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        @Override
        public Builder setAttributes(Map<String, String> attributes) {
            super.setAttributes(attributes);
            return this;
        }
    }
}
