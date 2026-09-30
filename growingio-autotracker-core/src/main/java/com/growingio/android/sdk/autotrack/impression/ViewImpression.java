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
import java.util.HashMap;
import java.util.Map;

/**
 * 一个视图上的一个曝光槽位。槽位以 identifier 区分，各自独立判定、独立发送。
 */
class ViewImpression {
    /** identifier 缺省时使用的固定槽位 key */
    static final String DEFAULT_SLOT = "$default";

    private final WeakReference<View> trackedView;
    private final String slot;
    private final String impressionEventName;
    private final String identifier;
    private final ImpressionConfig config;
    private Map<String, String> eventAttributes;

    /** 已发送过曝光，离开可视区再进入才会再发 */
    private boolean tracked = false;

    /** 连续可见的计时起点，0 表示当前不可见 */
    private long visibleSince = 0L;

    /** 停留时长复检的配对令牌：令牌变化即代表此前调度的复检已失效 */
    private int recheckToken = 0;

    ViewImpression(View trackedView, String slot, String impressionEventName,
                   Map<String, String> eventAttributes, String identifier, ImpressionConfig config) {
        this.trackedView = new WeakReference<>(trackedView);
        this.slot = slot;
        this.impressionEventName = impressionEventName;
        this.identifier = identifier;
        this.config = config;
        this.eventAttributes = copyAttributes(eventAttributes);
    }

    View getTrackedView() {
        return trackedView.get();
    }

    String getSlot() {
        return slot;
    }

    String getImpressionEventName() {
        return impressionEventName;
    }

    String getIdentifier() {
        return identifier;
    }

    ImpressionConfig getConfig() {
        return config;
    }

    Map<String, String> getEventAttributes() {
        return eventAttributes;
    }

    void setEventAttributes(Map<String, String> eventAttributes) {
        this.eventAttributes = copyAttributes(eventAttributes);
    }

    boolean isTracked() {
        return tracked;
    }

    void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    long getVisibleSince() {
        return visibleSince;
    }

    void setVisibleSince(long visibleSince) {
        this.visibleSince = visibleSince;
    }

    int getRecheckToken() {
        return recheckToken;
    }

    /**
     * 作废此前调度的停留时长复检，返回新的令牌。
     */
    int invalidateRecheck() {
        recheckToken++;
        return recheckToken;
    }

    /**
     * 事件名、属性、配置是否与传入的完全一致。列表刷新对可见元素原样重标时，
     * 三者都没变化就保留原有曝光状态，避免重复发送。
     */
    boolean matches(String eventName, Map<String, String> attributes, ImpressionConfig config) {
        if (!impressionEventName.equals(eventName)) {
            return false;
        }
        if (!this.config.equals(config)) {
            return false;
        }
        int selfSize = eventAttributes == null ? 0 : eventAttributes.size();
        int otherSize = attributes == null ? 0 : attributes.size();
        if (selfSize != otherSize) {
            return false;
        }
        return selfSize == 0 || eventAttributes.equals(attributes);
    }

    private static Map<String, String> copyAttributes(Map<String, String> attributes) {
        return attributes == null ? null : new HashMap<>(attributes);
    }
}
