/*
 * Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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

/**
 * 曝光条件。可以按元素单独配置，也可以通过
 * {@link com.growingio.android.sdk.autotrack.AutotrackConfig#setImpressionConfig(ImpressionConfig)} 配全局默认值。
 * <p>优先级：单元素 config &gt; 全局 config &gt; 默认值。
 */
public class ImpressionConfig {

    private float impressionScale = 0f;
    private long stayDuration = 0L;
    private boolean repeatable = true;

    public ImpressionConfig() {
    }

    public static ImpressionConfig create(float impressionScale, long stayDuration, boolean repeatable) {
        return new ImpressionConfig()
                .setImpressionScale(impressionScale)
                .setStayDuration(stayDuration)
                .setRepeatable(repeatable);
    }

    /**
     * 可见面积占元素自身面积的比例阈值，有效范围 [0,1]，默认 0，即露出即算曝光。
     */
    public ImpressionConfig setImpressionScale(float impressionScale) {
        if (impressionScale < 0) {
            impressionScale = 0;
        } else if (impressionScale > 1) {
            impressionScale = 1;
        }
        this.impressionScale = impressionScale;
        return this;
    }

    public float getImpressionScale() {
        return impressionScale;
    }

    /**
     * 连续可见需要满足的最小时长，单位毫秒，默认 0，即无需停留。滑过的元素不会曝光。
     */
    public ImpressionConfig setStayDuration(long stayDuration) {
        this.stayDuration = Math.max(stayDuration, 0L);
        return this;
    }

    public long getStayDuration() {
        return stayDuration;
    }

    /**
     * 是否允许同一元素多次曝光，默认 true。
     * <p>置为 false 时必须指定 identifier，否则会被降级为 true 并输出告警日志。
     */
    public ImpressionConfig setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
        return this;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public ImpressionConfig copy() {
        return create(impressionScale, stayDuration, repeatable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImpressionConfig)) {
            return false;
        }
        ImpressionConfig other = (ImpressionConfig) obj;
        return Float.compare(impressionScale, other.impressionScale) == 0
                && stayDuration == other.stayDuration
                && repeatable == other.repeatable;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(impressionScale);
        result = 31 * result + (int) (stayDuration ^ (stayDuration >>> 32));
        result = 31 * result + (repeatable ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ImpressionConfig{impressionScale=" + impressionScale
                + ", stayDuration=" + stayDuration
                + ", repeatable=" + repeatable + '}';
    }
}
