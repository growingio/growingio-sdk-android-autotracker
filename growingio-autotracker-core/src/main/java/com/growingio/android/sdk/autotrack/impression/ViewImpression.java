/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.autotrack.impression;

import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Map;

class ViewImpression {
    private final WeakReference<View> mTrackedView;
    private final String mImpressionEventName;
    private final Map<String, String> mEventAttributes;
    private boolean mLastVisible = false;

    ViewImpression(View trackedView, String impressionEventName, Map<String, String> eventAttributes) {
        mTrackedView = new WeakReference<>(trackedView);
        mImpressionEventName = impressionEventName;
        mEventAttributes = eventAttributes;
    }

    View getTrackedView() {
        return mTrackedView.get();
    }

    String getImpressionEventName() {
        return mImpressionEventName;
    }

    Map<String, String> getEventAttributes() {
        return mEventAttributes;
    }

    public boolean isLastVisible() {
        return mLastVisible;
    }

    public ViewImpression setLastVisible(boolean lastVisible) {
        mLastVisible = lastVisible;
        return this;
    }
}
