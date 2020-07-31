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

class ViewElement {
    private static final String NODE_TYPE_BUTTON = "button";
    private static final String NODE_TYPE_WEB_VIEW = "webView";

    private final String mXpath;
    private final String mParentXPath;
    private final int mLeft;
    private final int mTop;
    private final int mWidth;
    private final int mHeight;
    private final boolean mIsContainer;
    private final String mNodeType;
    private final String mContent;
    private final String mPage;
    private final int mZLevel;
    private final JSONObject mWebView;

    private ViewElement(Builder builder) {
        mXpath = builder.mXpath;
        mParentXPath = builder.mParentXPath;
        mLeft = builder.mLeft;
        mTop = builder.mTop;
        mWidth = builder.mWidth;
        mHeight = builder.mHeight;
        mIsContainer = builder.mIsContainer;
        mNodeType = builder.mNodeType;
        mContent = builder.mContent;
        mPage = builder.mPage;
        mZLevel = builder.mZLevel;
        mWebView = builder.mWebView;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("xpath", mXpath);
            json.put("parentXPath", mParentXPath);
            json.put("left", mLeft);
            json.put("top", mTop);
            json.put("width", mWidth);
            json.put("height", mHeight);
            json.put("isContainer", mIsContainer);
            json.put("nodeType", mNodeType);
            json.put("content", mContent);
            json.put("page", mPage);
            json.put("zLevel", mZLevel);
            json.put("webView", mWebView);
        } catch (JSONException ignored) {
        }
        return json;
    }

    static final class Builder {
        private String mXpath;
        private String mParentXPath;
        private int mLeft;
        private int mTop;
        private int mWidth;
        private int mHeight;
        private boolean mIsContainer;
        private String mNodeType;
        private String mContent;
        private String mPage;
        private int mZLevel;
        private JSONObject mWebView;

        public Builder setXpath(String xpath) {
            mXpath = xpath;
            return this;
        }

        public Builder setParentXPath(String parentXPath) {
            mParentXPath = parentXPath;
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

        public Builder setContainer(boolean container) {
            mIsContainer = container;
            return this;
        }

        public Builder setNodeType(String nodeType) {
            mNodeType = nodeType;
            return this;
        }

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Builder setPage(String page) {
            mPage = page;
            return this;
        }

        public Builder setZLevel(int zLevel) {
            mZLevel = zLevel;
            return this;
        }

        public Builder setWebView(JSONObject webView) {
            mWebView = webView;
            return this;
        }

        public ViewElement build() {
            return new ViewElement(this);
        }
    }
}
