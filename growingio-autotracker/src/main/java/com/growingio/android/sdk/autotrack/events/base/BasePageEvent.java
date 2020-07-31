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

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.events.base.BaseEventWithSequenceId;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.NetworkStatusProvider;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BasePageEvent extends BaseEventWithSequenceId {

    private final String mPageName;
    private final String mOrientation;
    private final String mTitle;
    private final String mNetworkState;
    private final String mReferralPage;

    protected BasePageEvent(EventBuilder<?> eventBuilder) {
        super(eventBuilder);
        mPageName = eventBuilder.mPageName;
        mOrientation = eventBuilder.mOrientation;
        mTitle = eventBuilder.mTitle;
        mNetworkState = eventBuilder.mNetworkState;
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

    public String getNetworkState() {
        return mNetworkState;
    }

    public String getReferralPage() {
        return mReferralPage;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mPageName", mPageName);
            json.put("mOrientation", mOrientation);
            json.put("mTitle", mTitle);
            json.put("mNetworkState", mNetworkState);
            json.put("mReferralPage", mReferralPage);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class EventBuilder<T extends BasePageEvent> extends BaseEventWithSequenceId.EventBuilder<T> {
        private String mPageName;
        private String mOrientation;
        private String mTitle;
        private String mNetworkState;
        private String mReferralPage;

        public EventBuilder(CoreAppState coreAppState) {
            super(coreAppState);
            mReferralPage = "";
            Activity activity = ActivityStateProvider.ActivityStatePolicy.get().getResumedActivity();
            if (activity != null) {
                mOrientation = activity.getResources().getConfiguration().orientation == 1
                        ? "PORTRAIT" : "LANDSCAPE";
            }
        }

        @Override
        public void readPropertyInGMain() {
            super.readPropertyInGMain();
            NetworkStatusProvider provider = NetworkStatusProvider.NetworkStatus.get(mCoreAppState.getGlobalContext());
            provider.checkNetStatus();
            mNetworkState = provider.getNetworkName();
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
        public EventType getEventType() {
            return EventType.PAGE;
        }

        public EventBuilder<T> setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }
    }
}
