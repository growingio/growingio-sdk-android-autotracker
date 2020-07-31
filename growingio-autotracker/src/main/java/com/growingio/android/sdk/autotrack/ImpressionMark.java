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

import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.utils.LogUtil;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class ImpressionMark {

    private final WeakReference<View> mView;
    private final String mEventId;

    private Number mNum;
    private JSONObject mVariable;
    private long mDelayTime;
    private String mGlobalId;
    private boolean mCollectV = true;          // 默认采集元素内容
    private float mVisibleScale = 0;           // 默认: 任何像素可见就算可见

    public ImpressionMark(View view, String eventId) {
        this.mView = new WeakReference<>(view);
        this.mEventId = eventId;
    }

    public View getView() {
        return mView.get();
    }

    public String getGlobalId() {
        return mGlobalId;
    }

    public ImpressionMark setGlobalId(String globalId) {
        this.mGlobalId = globalId;
        return this;
    }

    public String getEventId() {
        return mEventId;
    }

    public Number getNum() {
        return mNum;
    }

    /**
     * @deprecated 官网没有设置Num之处， 下个API变更变更删除此API
     */
    @Deprecated
    public ImpressionMark setNum(Number num) {
        this.mNum = num;
        return this;
    }

    public JSONObject getVariable() {
        return mVariable;
    }

    public ImpressionMark setVariable(JSONObject variable) {
        this.mVariable = variable;
        return this;
    }

    public long getDelayTimeMills() {
        return mDelayTime;
    }

    public ImpressionMark setDelayTimeMills(long delayTime) {
        this.mDelayTime = delayTime;
        return this;
    }

    public boolean isCollectContent() {
        return this.mCollectV;
    }

    public ImpressionMark setCollectContent(boolean collectV) {
        this.mCollectV = collectV;
        return this;
    }

    public float getVisibleScale() {
        return mVisibleScale;
    }

    /**
     * 设置有效曝光比例, 当曝光比例大于等于visibleScale时算View可见
     * 当可见像素值 / 总像素值 >= visibleScale 时认为是有效曝光
     *
     * @param visibleScale 有效曝光比例, 0 -- 任意像素可见为有效曝光, 1 -- 全部像素可见时为有效曝光
     */
    public ImpressionMark setVisibleScale(@FloatRange(from = 0.0f, to = 1.0f) float visibleScale) {
        if (visibleScale < 0 || visibleScale > 1) {
            String errorMsg = "visibleScale 区间为[0, 1], current visibleScale is " + visibleScale;
            if (GConfig.getInstance().debug()) {
                throw new IllegalArgumentException(errorMsg);
            } else {
                LogUtil.e("GIO.ImpressionMark", errorMsg);
            }
            return this;
        }
        this.mVisibleScale = visibleScale;
        return this;
    }
}
