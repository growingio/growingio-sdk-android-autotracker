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

import com.growingio.android.sdk.track.utils.LinkedString;

public class ViewNode {
    public static final String ANONYMOUS_CLASS_NAME = "Anonymous";
    private static final String TAG = "GIO.ViewNode";
    public View view;
    public int lastListPos = -1;
    public boolean fullScreen;
    public boolean hasListParent;
    public boolean inClickableGroup;
    public LinkedString parentXPath;
    public LinkedString originalParentXpath;
    public String windowPrefix;
    public String bannerText;
    public String viewContent;
    public boolean parentIdSettled = false;
    public LinkedString clickableParentXPath;
    private int mHashCode = -1;

    public ViewNode() {
    }

    /**
     * 在基本信息已经计算出来的基础上计算XPath,用于ViewHelper构建Xpath,和基于parentView构建Xpath
     */
    public ViewNode(View view, int lastListPos, boolean hasListParent, boolean fullScreen,
                    boolean inClickableGroup, boolean parentIdSettled, LinkedString originalParentXPath, LinkedString parentXPath, String windowPrefix) {

        this.view = view;
        this.lastListPos = lastListPos;
        this.fullScreen = fullScreen;
        this.hasListParent = hasListParent;
        this.inClickableGroup = inClickableGroup;
        this.parentIdSettled = parentIdSettled;
        this.parentXPath = parentXPath;
        originalParentXpath = originalParentXPath;
        this.windowPrefix = windowPrefix;
    }

    @Override
    public int hashCode() {
        if (mHashCode == -1) {
            int result = 17;
            result = result * 31 + (viewContent != null ? viewContent.hashCode() : 0);
            result = result * 31 + (parentXPath != null ? parentXPath.hashCode() : 0);
            result = result * 31 + lastListPos;
            mHashCode = result;
        }
        return mHashCode;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ViewNode && object.hashCode() == this.hashCode();
    }
}