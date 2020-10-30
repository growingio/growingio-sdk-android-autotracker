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

package com.growingio.android.sdk.autotrack.webservices.circle.entity;

import org.json.JSONException;
import org.json.JSONObject;

class PageElement {
    private final String mPath;
    private final String mTitle;
    private final int mLeft;
    private final int mTop;
    private final int mWidth;
    private final int mHeight;
    private final boolean mIsIgnored;

    private PageElement(Builder builder) {
        mPath = builder.mPath;
        mTitle = builder.mTitle;
        mLeft = builder.mLeft;
        mTop = builder.mTop;
        mWidth = builder.mWidth;
        mHeight = builder.mHeight;
        mIsIgnored = builder.mIsIgnored;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("path", mPath);
            json.put("title", mTitle);
            json.put("left", mLeft);
            json.put("top", mTop);
            json.put("width", mWidth);
            json.put("height", mHeight);
            json.put("isIgnored", mIsIgnored);
        } catch (JSONException ignored) {
        }
        return json;
    }

    static final class Builder {
        private String mPath;
        private String mTitle;
        private int mLeft;
        private int mTop;
        private int mWidth;
        private int mHeight;
        private boolean mIsIgnored;

        public Builder setPath(String path) {
            mPath = path;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setLeft(int left) {
            mLeft = left;
            return this;
        }

        public Builder setTop(int top) {
            mTop = top;
            return this;
        }

        public Builder setWidth(int width) {
            mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            mHeight = height;
            return this;
        }

        public Builder setIgnored(boolean ignored) {
            mIsIgnored = ignored;
            return this;
        }

        public PageElement build() {
            return new PageElement(this);
        }
    }
}
