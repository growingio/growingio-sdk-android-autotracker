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

import android.support.annotation.StringDef;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.WindowHelper;
import com.growingio.android.sdk.autotrack.webservices.circle.ViewUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class ViewNode {
    private View mView;
    private String mXPath;
    private String mOriginalXPath;
    private String mClickableParentXPath;
    private String mViewContent;
    private boolean mHasListParent;
    private int mIndex;

    @Retention(SOURCE)
    @StringDef({
            INPUT,
            TEXT,
            WEB_VIEW,
            BUTTON,
            LIST
    })
    private @interface NodeType {
    }
    private static final String INPUT = "INPUT";
    private static final String TEXT = "TEXT";
    private static final String WEB_VIEW = "WEB_VIEW";
    private static final String BUTTON = "BUTTON";
    private static final String LIST = "LIST";

    private ViewNode() {
    }

    public View getView() {
        return mView;
    }

    public String getXPath() {
        return mXPath;
    }

    public String getClickableParentXPath() {
        return mClickableParentXPath;
    }

    public String getViewContent() {
        return mViewContent;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getNodeType() {
        if (mView instanceof EditText) {
            return INPUT;
        }

        if (mView instanceof TextView) {
            return TEXT;
        }

        if (ClassExistHelper.isListView(mView.getParent())) {
            return LIST;
        }

        if (ClassExistHelper.isWebView(mView)) {
            return WEB_VIEW;
        }

        return BUTTON;
    }

    public ViewNode appendNode(View view) {
        if (mView instanceof ViewGroup) {
            return appendNode(view, ((ViewGroup) mView).indexOfChild(view));
        }

        return this;
    }

    public ViewNode appendNode(View view, int index) {
        boolean hasListParent = mHasListParent || ClassExistHelper.isListView(view);
        return ViewNodeBuilder.newViewNode()
                .setView(view)
                .setIndex(hasListParent ? mIndex : -1)
                .setXPath(mXPath)
                .setOriginalXPath(mOriginalXPath)
                .setClickableParentXPath(ViewUtil.canCircle(mView) ? mXPath : mClickableParentXPath)
                .setHasListParent(hasListParent)
                .setViewPosition(index)
                .needRecalculate(true)
                .build();
    }

    public static final class ViewNodeBuilder {
        private View mView;
        private String mXPath;
        private String mOriginalXPath;
        private String mClickableParentXPath;
        private String mViewContent;
        private boolean mHasListParent;
        private int mIndex;

        private boolean mNeedRecalculate = false;
        private int mViewPosition;

        private ViewNodeBuilder() {
        }

        public static ViewNodeBuilder newViewNode() {
            return new ViewNodeBuilder();
        }

        public ViewNodeBuilder needRecalculate(boolean needRecalculate) {
            this.mNeedRecalculate = needRecalculate;
            return this;
        }

        public ViewNodeBuilder setViewPosition(int viewPosition) {
            this.mViewPosition = viewPosition;
            return this;
        }

        public ViewNodeBuilder setView(View mView) {
            this.mView = mView;
            return this;
        }

        public ViewNodeBuilder setXPath(String mXPath) {
            this.mXPath = mXPath;
            return this;
        }

        public ViewNodeBuilder setOriginalXPath(String mOriginalXPath) {
            this.mOriginalXPath = mOriginalXPath;
            return this;
        }

        public ViewNodeBuilder setClickableParentXPath(String mClickableParentXPath) {
            this.mClickableParentXPath = mClickableParentXPath;
            return this;
        }

        public ViewNodeBuilder setViewContent(String mViewContent) {
            this.mViewContent = mViewContent;
            return this;
        }

        public ViewNodeBuilder setHasListParent(boolean hasListParent) {
            this.mHasListParent = hasListParent;
            return this;
        }

        public ViewNodeBuilder setIndex(int mIndex) {
            this.mIndex = mIndex;
            return this;
        }

        public ViewNode build() {
            if (mNeedRecalculate) {
                recalculate();
            }

            ViewNode viewNode = new ViewNode();
            viewNode.mView = this.mView;
            viewNode.mXPath = this.mXPath;
            viewNode.mOriginalXPath = this.mOriginalXPath;
            viewNode.mClickableParentXPath = this.mClickableParentXPath;
            viewNode.mViewContent = this.mViewContent;
            viewNode.mHasListParent = this.mHasListParent;
            viewNode.mIndex = this.mIndex;
            return viewNode;
        }

        private void recalculate() {
            calculateViewPosition();
            calculateViewXPath();
            calculateViewContent();
        }

        private void calculateViewPosition() {
            if (mView.getParent() != null && (mView.getParent() instanceof ViewGroup)) {
                ViewGroup parent = (ViewGroup) mView.getParent();
                if (ClassExistHelper.instanceOfAndroidXViewPager(parent)) {
                    mViewPosition = ((androidx.viewpager.widget.ViewPager) parent).getCurrentItem();
                } else if (ClassExistHelper.instanceOfSupportViewPager(parent)) {
                    mViewPosition = ((android.support.v4.view.ViewPager) parent).getCurrentItem();
                } else if (parent instanceof AdapterView) {
                    AdapterView listView = (AdapterView) parent;
                    mViewPosition = listView.getFirstVisiblePosition() + mViewPosition;
                } else if (ClassExistHelper.instanceOfRecyclerView(parent)) {
                    int adapterPosition = ViewHelper.getChildAdapterPositionInRecyclerView(mView, parent);
                    if (adapterPosition >= 0) {
                        mViewPosition = adapterPosition;
                    }
                }
            }
        }

        private void calculateViewXPath() {
            Object parentObject = mView.getParent();
            if (parentObject == null || (WindowHelper.isDecorView(mView) && !(parentObject instanceof View))) {
                return;
            }

            String customId = ViewAttributeUtil.getCustomId(mView);
            Page<?> page = ViewAttributeUtil.getViewPage(mView);
            if (customId != null) {
                mOriginalXPath = "/" + customId;
                mXPath = mOriginalXPath;
                return;
            } else if (page != null && !page.isIgnored()) {
                mOriginalXPath = WindowHelper.getWindowPrefix(mView);
                mXPath = mOriginalXPath;
                return;
            }

            StringBuilder originalXPath = new StringBuilder(mOriginalXPath);
            StringBuilder xPath = new StringBuilder(mXPath);
            String viewName = ClassUtil.getSimpleClassName(mView.getClass());

            if (parentObject instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) parentObject;
                if (parent instanceof ExpandableListView) {
                    ExpandableListView listParent = (ExpandableListView) parent;
                    long elp = ((ExpandableListView) mView.getParent()).getExpandableListPosition(mViewPosition);
                    if (ExpandableListView.getPackedPositionType(elp) == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
                        if (mViewPosition < listParent.getHeaderViewsCount()) {
                            originalXPath.append("/ELH[").append(mViewPosition).append("]/").append(viewName).append("[0]");
                            xPath.append("/ELH[").append(mViewPosition).append("]/").append(viewName).append("[0]");
                        } else {
                            int footerIndex = mViewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                            originalXPath.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                            xPath.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                        }
                    } else {
                        int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
                        int childIdx = ExpandableListView.getPackedPositionChild(elp);
                        if (childIdx != -1) {
                            mIndex = childIdx;
                            xPath = new StringBuilder(originalXPath).append("/ELVG[").append(groupIdx).append("]/ELVC[-]/").append(viewName).append("[0]");
                            originalXPath.append("/ELVG[").append(groupIdx).append("]/ELVC[").append(childIdx).append("]/").append(viewName).append("[0]");
                        } else {
                            mIndex = groupIdx;
                            xPath = new StringBuilder(originalXPath).append("/ELVG[-]/").append(viewName).append("[0]");
                            originalXPath.append("/ELVG[").append(groupIdx).append("]/").append(viewName).append("[0]");
                        }
                    }
                } else if (ClassExistHelper.isListView(parent) || ClassExistHelper.instanceOfRecyclerView(parent)) {
                    mIndex = mViewPosition;
                    xPath = new StringBuilder(originalXPath).append("/").append(viewName).append("[-]");
                    originalXPath.append("/").append(viewName).append("[").append(mViewPosition).append("]");
                } else if (ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(parentObject)
                        || ClassExistHelper.instanceOfSupportSwipeRefreshLayout(parentObject)) {
                    originalXPath.append("/").append(viewName).append("[0]");
                    xPath.append("/").append(viewName).append("[0]");
                } else {
                    int matchTypePosition = 0;
                    String matchType = mView.getClass().getSimpleName();
                    boolean findChildView = false;
                    for (int siblingIndex = 0; siblingIndex < parent.getChildCount(); siblingIndex++) {
                        View siblingView = parent.getChildAt(siblingIndex);
                        if (siblingView == mView) {
                            findChildView = true;
                            break;
                        } else if (siblingView.getClass().getSimpleName().equals(matchType)) {
                            matchTypePosition++;
                        }
                    }
                    if (findChildView) {
                        originalXPath.append("/").append(viewName).append("[").append(matchTypePosition).append("]");
                        xPath.append("/").append(viewName).append("[").append(matchTypePosition).append("]");
                    } else {
                        originalXPath.append("/").append(viewName).append("[").append(mViewPosition).append("]");
                        xPath.append("/").append(viewName).append("[").append(mViewPosition).append("]");
                    }
                }
            } else {
                originalXPath.append("/").append(viewName).append("[").append(mViewPosition).append("]");
                xPath.append("/").append(viewName).append("[").append(mViewPosition).append("]");
            }

            String id = ViewHelper.getViewPackageId(mView);
            if (id != null) {
                originalXPath.append("#").append(id);
                xPath.append("#").append(id);
            }

            mXPath = xPath.toString();
            mOriginalXPath = originalXPath.toString();
        }

        private void calculateViewContent() {
            mViewContent = ViewHelper.getViewContent(mView);
        }
    }
}