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
import android.view.ViewParent;

import com.growingio.android.sdk.autotrack.util.ViewAttributeUtil;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.utils.LinkedString;
import com.growingio.android.sdk.track.utils.LogUtil;

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
    public String cid;
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

        if (GConfig.getInstance().isRnMode() && GConfig.getInstance().useRnOptimizedPath() && this.view != null) {
            identifyRNChangeablePath();
        }
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

    /**
     * 判断当前View是否有必要进行removeRNChangeablePath操作
     */
    private void identifyRNChangeablePath() {
        ViewParent parent = view.getParent();
        if (!(parent instanceof View)) {
            return;
        }

        boolean shouldRemoveChangeablePath = false;

        View viewParent = (View) parent;
        String viewRNPage = ViewAttributeUtil.getRnPageKey(view);
        String viewParentRNPage = ViewAttributeUtil.getRnPageKey(viewParent);
        LogUtil.d("GIO.HandleRNView", "IdentifyRNChangeablePath: ", view.getClass().getName());
        LogUtil.d("GIO.HandleRNView", "mParentXPath: ", parentXPath);
        LogUtil.d("GIO.HandleRNView", "viewRNPage: ", viewRNPage);

        if (viewRNPage != null) {
            if (!viewRNPage.equals(viewParentRNPage)) {
                shouldRemoveChangeablePath = true;
            }
        } else {
            if (viewParentRNPage != null) {
                shouldRemoveChangeablePath = true;
            }
        }

        if (shouldRemoveChangeablePath) {
            LogUtil.d("GIO.HandleRNView", "viewParentRNPage: ", viewParentRNPage);
            removeRNChangeablePath();
        }
    }

    /**
     * 此方法用于移除与父元素有不同GROWING_RN_VIEW_PAGE_KEY属性的元素的mParentXPath,并将对应的mViewIndex置为0
     * 因为React Native中Navigator中每个子项是由ViewGroup包含的,而这个ViewGroup是被隐藏同时不可点击的,
     * 所以对于React Native而言子元素和父元素如果GROWING_RN_VIEW_PAGE_KEY不同必然属于Imp元素而非Click元素
     */
    private void removeRNChangeablePath() {
        parentXPath = new LinkedString();
        originalParentXpath = new LinkedString();
    }
}
