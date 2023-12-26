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
package com.growingio.android.sdk.collection;

import android.view.View;

import androidx.annotation.FloatRange;

import org.json.JSONObject;

@Deprecated
public class ImpressionMark {
    public ImpressionMark(View view, String eventId){
    }

    @Deprecated
    public View getView() {
        return null;
    }

    @Deprecated
    public ImpressionMark setGlobalId(String globalId) {
        return this;
    }

    @Deprecated
    public String getGlobalId() {
        return "";
    }

    @Deprecated
    public String getEventId() {
        return "";
    }

    @Deprecated
    public Number getNum() {
        return 0;
    }

    @Deprecated
    public ImpressionMark setNum(Number num) {
        return this;
    }

    @Deprecated
    public JSONObject getVariable() {
        return null;
    }

    @Deprecated
    public ImpressionMark setVariable(JSONObject variable) {
        return this;
    }

    @Deprecated
    public long getDelayTimeMills() {
        return 0;
    }

    @Deprecated
    public ImpressionMark setDelayTimeMills(long delayTime) {
        return this;
    }

    @Deprecated
    public ImpressionMark setCollectContent(boolean collectV) {
        return this;
    }

    @Deprecated
    public boolean isCollectContent(){
        return false;
    }

    @Deprecated
    public ImpressionMark setVisibleScale(@FloatRange(from = 0.0f, to = 1.0f) float visibleScale) {
        return this;
    }

    @Deprecated
    public float getVisibleScale() {
        return 0f;
    }
}
