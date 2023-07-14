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

import android.content.res.Configuration;

import androidx.annotation.StringDef;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.sdk.annotation.json.JsonSerializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@JsonSerializer
public class PageEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
    public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ORIENTATION_PORTRAIT, ORIENTATION_LANDSCAPE})
    public @interface Orientation {
    }

    private final String path;
    private final String orientation;
    private final String title;
    private final String referralPage;

    protected PageEvent(Builder eventBuilder) {
        super(eventBuilder);
        path = eventBuilder.path;
        orientation = eventBuilder.orientation;
        title = eventBuilder.title;
        referralPage = eventBuilder.referralPage;
    }

    public String getPath() {
        return checkValueSafe(path);
    }

    public String getOrientation() {
        return checkValueSafe(orientation);
    }

    public String getTitle() {
        return checkValueSafe(title);
    }

    public String getReferralPage() {
        return checkValueSafe(referralPage);
    }

    public static class Builder extends BaseAttributesEvent.Builder<PageEvent> {
        private String path;
        private String orientation;
        private String title;
        private String referralPage = "";

        public Builder() {
            super(AutotrackEventType.PAGE);
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setReferralPage(String referralPage) {
            this.referralPage = referralPage;
            return this;
        }

        public Builder setOrientation(@Orientation String orientation) {
            this.orientation = orientation;
            return this;
        }

        public String getPath() {
            return path;
        }

        @Override
        public PageEvent build() {
            return new PageEvent(this);
        }

        @Override
        public void readPropertyInTrackThread() {
            if (orientation == null) {
                orientation = TrackerContext.get().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PageEvent.ORIENTATION_PORTRAIT : PageEvent.ORIENTATION_LANDSCAPE;
            }
            super.readPropertyInTrackThread();
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}
