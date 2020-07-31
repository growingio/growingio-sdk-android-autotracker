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

package com.growingio.android.sdk.autotrack.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Screenshot implements Parcelable {

    public static final Creator<Screenshot> CREATOR = new Creator<Screenshot>() {
        @Override
        public Screenshot createFromParcel(Parcel source) {
            return new Screenshot(source);
        }

        @Override
        public Screenshot[] newArray(int size) {
            return new Screenshot[size];
        }
    };
    private String mX;
    private String mY;
    private String mW;
    private String mH;
    private String mTarget;
    private String mViewport;

    public Screenshot() {
    }

    protected Screenshot(Parcel in) {
        this.mX = in.readString();
        this.mY = in.readString();
        this.mW = in.readString();
        this.mH = in.readString();
        this.mTarget = in.readString();
        this.mViewport = in.readString();
    }

    public static Screenshot parse(JSONObject jsonObject) {
        Screenshot screenshot = new Screenshot();
        try {
            screenshot.mX = jsonObject.getString("x");
            screenshot.mY = jsonObject.getString("y");
            screenshot.mW = jsonObject.getString("w");
            screenshot.mH = jsonObject.getString("h");
            screenshot.mTarget = jsonObject.getString("target");
            screenshot.mViewport = jsonObject.getString("viewport");
        } catch (JSONException ignored) {
        }
        return screenshot;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", mX);
            jsonObject.put("y", mY);
            jsonObject.put("w", mW);
            jsonObject.put("h", mH);
            jsonObject.put("target", mTarget);
            jsonObject.put("viewport", mViewport);
        } catch (JSONException ignored) {
        }
        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mX);
        dest.writeString(this.mY);
        dest.writeString(this.mW);
        dest.writeString(this.mH);
        dest.writeString(this.mTarget);
        dest.writeString(this.mViewport);
    }
}

