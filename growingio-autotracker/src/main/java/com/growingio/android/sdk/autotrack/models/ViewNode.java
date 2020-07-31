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

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.growingio.android.sdk.autotrack.util.Util;
import com.growingio.android.sdk.autotrack.util.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.util.ViewHelper;
import com.growingio.android.sdk.autotrack.util.WindowHelper;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.LinkedString;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.util.List;

public class ViewNode {
    public static final String ANONYMOUS_CLASS_NAME = "Anonymous";
    private static final String TAG = "GIO.ViewNode";
    public View view;
    public int lastListPos = -1;
    public boolean fullScreen;
    public boolean hasListParent;
    public boolean inClickableGroup;
    public int viewPosition;
    public LinkedString parentXPath;
    public LinkedString originalParentXpath;
    public String windowPrefix;
    public String viewName;
    public Screenshot screenshot;
    public String bannerText;
    public String viewContent;
    public boolean parentIdSettled = false;
    public Rect clipRect;
    public LinkedString clickableParentXPath;
    public String imageViewDHashCode;
    public String patternXPath;
    public String cid;
    // h5 传递过来的elem中包含了isTrackingEditText属性, 必须有个地方进行保存, 作为临时变量
    // 约定hybrid 开头的变量， native元素不能使用
    // https://growingio.atlassian.net/browse/PI-14177
    public boolean hybridIsTrackingEditText = false;
    ViewTraveler mViewTraveler;
    private int mViewIndex;
    private int mHashCode = -1;

    public ViewNode() {
    }

    /**
     * 在基本信息已经计算出来的基础上计算XPath,用于ViewHelper构建Xpath,和基于parentView构建Xpath
     */
    public ViewNode(View view, int viewIndex, int lastListPos, boolean hasListParent, boolean fullScreen,
                    boolean inClickableGroup, boolean parentIdSettled, LinkedString originalParentXPath, LinkedString parentXPath, String windowPrefix, ViewTraveler viewTraveler) {

        this.view = view;
        this.lastListPos = lastListPos;
        this.fullScreen = fullScreen;
        mViewIndex = viewIndex;
        this.hasListParent = hasListParent;
        this.inClickableGroup = inClickableGroup;
        this.parentIdSettled = parentIdSettled;
        this.parentXPath = parentXPath;
        originalParentXpath = originalParentXPath;
        this.windowPrefix = windowPrefix;

        mViewTraveler = viewTraveler;

        if (GConfig.getInstance().isRnMode() && GConfig.getInstance().useRnOptimizedPath() && this.view != null) {
            identifyRNChangeablePath();
        }
    }

    public void setViewTraveler(ViewTraveler viewTraveler) {
        this.mViewTraveler = viewTraveler;
    }

    public void traverseViewsRecur() {
        if (mViewTraveler != null && mViewTraveler.needTraverse(this)) {
            viewName = Util.getSimpleClassName(view.getClass());
            viewPosition();
            calcXPath();
            viewContent();

            if (needTrack()) {
                mViewTraveler.traverseCallBack(this);
            }
            if (ClassExistHelper.instanceOfX5WebView(view)) {
                return;
            }
            traverseChildren();
        }
    }

    public void traverseChildren() {
        if (view instanceof ViewGroup && !(view instanceof Spinner)) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                ViewNode childViewNode = new ViewNode(childView, i, lastListPos,
                        hasListParent || Util.isListView(view), fullScreen,
                        inClickableGroup || Util.isViewClickable(view), parentIdSettled,
                        LinkedString.copy(originalParentXpath),
                        LinkedString.copy(parentXPath), windowPrefix, mViewTraveler);
                childViewNode.cid = ViewAttributeUtil.getCustomId(childView);
                if (Util.isViewClickable(view)) {
                    childViewNode.clickableParentXPath = parentXPath;
                } else {
                    childViewNode.clickableParentXPath = clickableParentXPath;
                }
                childViewNode.bannerText = bannerText;
                /**
                 * 在traverseViewsRecur之前childViewNode的XPath等信息还是parentView的，而不是childViewNode的；
                 */
                childViewNode.traverseViewsRecur();
            }
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

    public ViewNode copyWithoutView() {
        return new ViewNode(null, mViewIndex, lastListPos, hasListParent,
                fullScreen, inClickableGroup, parentIdSettled,
                LinkedString.fromString(originalParentXpath.toStringValue()),
                LinkedString.fromString(parentXPath.toStringValue()), windowPrefix, null);
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public boolean isNeedTrack() {
        return ViewHelper.isViewSelfVisible(view) && !Util.isIgnoredView(view);
    }

    public boolean isIgnoreImp() {
        return ViewAttributeUtil.getIgnoreImpKey(view) != null;
    }

    private void viewContent() {
        viewContent = Util.getViewContent(view, bannerText);
    }

    private void viewPosition() {
        int idx = mViewIndex;
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (ClassExistHelper.instanceOfAndroidXViewPager(parent)) {
                idx = ((androidx.viewpager.widget.ViewPager) parent).getCurrentItem();
            } else if (ClassExistHelper.instanceOfSupportViewPager(parent)) {
                idx = ((ViewPager) parent).getCurrentItem();
            } else if (parent instanceof AdapterView) {
                AdapterView listView = (AdapterView) parent;
                idx = listView.getFirstVisiblePosition() + mViewIndex;
            } else if (ClassExistHelper.instanceOfRecyclerView(parent)) {
                int adapterPosition = ViewHelper.getChildAdapterPositionInRecyclerView(view, parent);
                if (adapterPosition >= 0) {
                    idx = adapterPosition;
                }
            }
        }
        viewPosition = idx;
    }

    private void calcXPath() {
        ViewParent parentView = view.getParent();
        if (parentView == null || (WindowHelper.isDecorView(view) && !(parentView instanceof View))) {
            return;
        }
        String viewName = ViewAttributeUtil.getViewNameKey(view);
        if (viewName != null) {
            originalParentXpath = LinkedString.fromString("/").append(viewName);
            parentXPath.append("/").append(viewName);
            return;
        }

        if (parentView instanceof View) {
            View parent = (View) parentView;

            if (parent instanceof ExpandableListView) {
                // 处理ExpandableListView
                ExpandableListView listParent = (ExpandableListView) parent;
                long elp = ((ExpandableListView) view.getParent()).getExpandableListPosition(viewPosition);
                if (ExpandableListView.getPackedPositionType(elp) == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
                    hasListParent = false;
                    if (viewPosition < listParent.getHeaderViewsCount()) {
                        originalParentXpath.append("/ELH[").append(viewPosition).append("]/").append(this.viewName).append("[0]");
                        parentXPath.append("/ELH[").append(viewPosition).append("]/").append(this.viewName).append("[0]");
                    } else {
                        int footerIndex = viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                        originalParentXpath.append("/ELF[").append(footerIndex).append("]/").append(this.viewName).append("[0]");
                        parentXPath.append("/ELF[").append(footerIndex).append("]/").append(this.viewName).append("[0]");
                    }
                } else {
                    int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
                    int childIdx = ExpandableListView.getPackedPositionChild(elp);
                    if (childIdx != -1) {
                        lastListPos = childIdx;
                        parentXPath = LinkedString.fromString(originalParentXpath.toStringValue()).append("/ELVG[").append(groupIdx).append("]/ELVC[-]/").append(this.viewName).append("[0]");
                        originalParentXpath.append("/ELVG[").append(groupIdx).append("]/ELVC[").append(childIdx).append("]/").append(this.viewName).append("[0]");
                    } else {
                        lastListPos = groupIdx;
                        parentXPath = LinkedString.fromString(originalParentXpath.toStringValue()).append("/ELVG[-]/").append(this.viewName).append("[0]");
                        originalParentXpath.append("/ELVG[").append(groupIdx).append("]/").append(this.viewName).append("[0]");
                    }
                }
            } else if (Util.isListView(parent)) {
                // 处理有特殊的position的元素

                List bannerTag = ViewAttributeUtil.getBannerKey(parent);
                if (bannerTag != null && (bannerTag).size() > 0) {
                    viewPosition = Util.calcBannerItemPosition((List) bannerTag, viewPosition);
                    bannerText = Util.truncateViewContent(String.valueOf(((List) bannerTag).get(viewPosition)));
                }
                lastListPos = viewPosition;
                parentXPath = LinkedString.fromString(originalParentXpath.toStringValue()).append("/").append(this.viewName).append("[-]");
                originalParentXpath.append("/").append(this.viewName).append("[").append(lastListPos).append("]");
            } else if (ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(parentView)
                    || ClassExistHelper.instanceOfSupportSwipeRefreshLayout(parentView)) {
                originalParentXpath.append("/").append(this.viewName).append("[0]");
                parentXPath.append("/").append(this.viewName).append("[0]");
            } else {
                originalParentXpath.append("/").append(this.viewName).append("[").append(viewPosition).append("]");
                parentXPath.append("/").append(this.viewName).append("[").append(viewPosition).append("]");
            }
        } else {
            originalParentXpath.append("/").append(this.viewName).append("[").append(viewPosition).append("]");
            parentXPath.append("/").append(this.viewName).append("[").append(viewPosition).append("]");
        }

        if (GConfig.getInstance().useID()) {
            String id = Util.getIdName(view, parentIdSettled);
            if (id != null) {
                if (ViewAttributeUtil.getViewId(view) != null) {
                    parentIdSettled = true;
                }
                originalParentXpath.append("#").append(id);
                parentXPath.append("#").append(id);
            }
        }
    }

    private boolean needTrack() {
        ViewParent parent = view.getParent();
        if (parent != null) {
            if (view.isClickable()
                    || view instanceof TextView
                    || view instanceof ImageView
                    || view instanceof WebView
                    || parent instanceof AdapterView
                    || parent instanceof RadioGroup
                    || view instanceof Spinner
                    || view instanceof RatingBar
                    || view instanceof SeekBar
                    || ClassExistHelper.instanceOfX5WebView(view)) {
                return true;
            }
        }
        return false;
    }


    public void getVisibleRect(View view, Rect rect, boolean fullscreen) {
        if (fullscreen) {
            view.getGlobalVisibleRect(rect);
        } else {
            int[] offset = new int[2];
            view.getLocationOnScreen(offset);
            view.getLocalVisibleRect(rect);
            rect.offset(offset[0], offset[1]);
        }
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
        mViewIndex = 0;
    }
}
