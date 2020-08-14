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

import android.view.View;

public class ViewNode {
    private final View mView;
    private final String mXPath;
    private final String mOriginalXPath;
    private final String mViewContent;
    private final int mIndex;

    public ViewNode(View view, String xpath, String originalXPath, String viewContent, int index) {
        mView = view;
        mXPath = xpath;
        mOriginalXPath = originalXPath;
        mViewContent = viewContent;
        mIndex = index;
    }

    public View getView() {
        return mView;
    }

    public String getXPath() {
        return mXPath;
    }

    public String getViewContent() {
        return mViewContent;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getOriginalXPath() {
        return mOriginalXPath;
    }
}