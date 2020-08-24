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

package com.growingio.android.sdk.autotrack.events.base;

import android.app.Activity;

import com.growingio.android.sdk.autotrack.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BasePageEvent extends BaseEvent {
    private static final String ORIENTATION_PORTRAIT = "PORTRAIT";
    private static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

    private final String mPageName;
    private final String mOrientation;
    private final String mTitle;
    private final String mReferralPage;

    protected BasePageEvent(EventBuilder<?> eventBuilder) {
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

    public abstract static class EventBuilder<T extends BasePageEvent> extends BaseEvent.BaseEventBuilder<T> {
        private String mPageName;
        private String mOrientation;
        private String mTitle;
        private String mReferralPage;

        public EventBuilder() {
            super();
            mReferralPage = "";
            Activity activity = ActivityStateProvider.get().getResumedActivity();
            if (activity != null) {
                mOrientation = activity.getResources().getConfiguration().orientation == 1
                        ? ORIENTATION_PORTRAIT : ORIENTATION_LANDSCAPE;
            }
        }

        public String getPageName() {
            return mPageName;
        }

        public EventBuilder<T> setPageName(String pageName) {
            mPageName = pageName;
            return this;
        }

        public String getTitle() {
            return mTitle;
        }

        public EventBuilder<T> setTitle(String title) {
            mTitle = title;
            return this;
        }

        public String getReferralPage() {
            return mReferralPage;
        }

        public EventBuilder<T> setReferralPage(String referralPage) {
            mReferralPage = referralPage;
            return this;
        }

        @Override
        public String getEventType() {
            return AutotrackEventType.PAGE;
        }

        public EventBuilder<T> setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }
    }
}
