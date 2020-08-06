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

package com.growingio.android.sdk.autotrack;

import android.view.View;

import androidx.annotation.FloatRange;

import java.lang.ref.WeakReference;
import java.util.Map;

public class ImpressionConfig {

    private final WeakReference<View> mView;
    private final String mEventName;

    private Map<String, String> mAttributes;
    private long mDelayTime;
    private String mGlobalId;
    private boolean mCollectV = true;          // 默认采集元素内容
    static private float sImpressionScale = 0; // 默认: 任何像素可见就算可见

    public ImpressionConfig(View view, String eventName, Map<String, String> attributes) {
        this.mView = new WeakReference<>(view);
        this.mEventName = eventName;
        this.mAttributes = attributes;
    }

    public View getView() {
        return mView.get();
    }

    public String getGlobalId() {
        return mGlobalId;
    }

    public ImpressionConfig setGlobalId(String globalId) {
        this.mGlobalId = globalId;
        return this;
    }

    public String getEventName() {
        return mEventName;
    }

    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    public long getDelayTimeMills() {
        return mDelayTime;
    }

    public ImpressionConfig setDelayTimeMills(long delayTime) {
        this.mDelayTime = delayTime;
        return this;
    }

    public boolean isCollectContent() {
        return this.mCollectV;
    }

    public ImpressionConfig setCollectContent(boolean collectV) {
        this.mCollectV = collectV;
        return this;
    }

    public static float getVisibleScale() {
        return sImpressionScale;
    }

    /**
     * 设置有效曝光比例, 当曝光比例大于等于visibleScale时算View可见
     * 当可见像素值 / 总像素值 >= visibleScale 时认为是有效曝光
     *
     * @param visibleScale 有效曝光比例, 0 -- 任意像素可见为有效曝光, 1 -- 全部像素可见时为有效曝光
     */
    public static void setVisibleScale(@FloatRange(from = 0.0f, to = 1.0f) float visibleScale) {
        if (visibleScale < 0 || visibleScale > 1) {
            String errorMsg = "visibleScale 区间为[0, 1], current visibleScale is " + visibleScale;
            throw new IllegalArgumentException(errorMsg);
        }
        sImpressionScale = visibleScale;
    }
}
