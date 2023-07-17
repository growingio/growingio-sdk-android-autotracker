/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.view;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.shadow.ListMenuItemViewShadow;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.view.WindowHelper;

/**
 * <p>
 *
 * @author cpacm 2023/7/12
 */
class ViewNodeV4 {
    private View view;
    private String xPath;

    private String xIndex;
    private String indeedXIndex;
    private String clickableParentXPath;
    private String clickablePatentXIndex;
    private String viewContent;
    private boolean hasListParent;
    private int index;

    private String page;

    private int viewPosition; //for xpath calculate

    private ViewNodeV4 parent;

    ViewNodeV4 withView(View view) {
        this.view = view;
        return this;
    }

    View getView() {
        return view;
    }

    ViewNodeV4 setXPath(String xPath) {
        this.xPath = xPath;
        return this;
    }

    String getXPath() {
        return xPath;
    }

    ViewNodeV4 setClickableParentXPath(String clickableParentXPath) {
        this.clickableParentXPath = clickableParentXPath;
        return this;
    }

    String getClickableParentXPath() {
        return clickableParentXPath;
    }

    public String getClickablePatentXIndex() {
        return clickablePatentXIndex;
    }

    public ViewNodeV4 setClickablePatentXIndex(String clickablePatentXIndex) {
        this.clickablePatentXIndex = clickablePatentXIndex;
        return this;
    }

    ViewNodeV4 setViewContent(String viewContent) {
        this.viewContent = viewContent;
        return this;
    }

    String getViewContent() {
        return viewContent;
    }

    ViewNodeV4 setHasListParent(boolean hasListParent) {
        this.hasListParent = hasListParent;
        return this;
    }

    boolean isHasListParent() {
        return hasListParent;
    }

    ViewNodeV4 setIndex(int index) {
        this.index = index;
        return this;
    }

    int getIndex() {
        return index;
    }

    public ViewNodeV4 setViewPosition(int viewPosition) {
        this.viewPosition = viewPosition;
        return this;
    }

    public ViewNodeV4 getParent() {
        return parent;
    }

    public ViewNodeV4 setParent(ViewNodeV4 parent) {
        this.parent = parent;
        return this;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public ViewNodeV4 setXIndex(String xIndex) {
        this.xIndex = xIndex;
        return this;
    }

    public String getXIndex() {
        return xIndex;
    }

    public String getIndeedXIndex() {
        return indeedXIndex;
    }

    public ViewNodeV4 setIndeedXIndex(String indeedXIndex) {
        this.indeedXIndex = indeedXIndex;
        return this;
    }

    public void calculate() {
        calculateViewPosition();
        calculateViewXPath();
        calculateViewContent();
    }

    private void calculateViewPosition() {
        if (this.view.getParent() != null && (this.view.getParent() instanceof ViewGroup)) {
            ViewGroup parent = (ViewGroup) (this.view.getParent());
            if (ClassExistHelper.instanceOfAndroidXViewPager(parent)) {
                this.viewPosition = ((androidx.viewpager.widget.ViewPager) parent).getCurrentItem();
            } else if (ClassExistHelper.instanceOfSupportViewPager(parent)) {
                this.viewPosition = ((android.support.v4.view.ViewPager) parent).getCurrentItem();
            } else if (parent instanceof AdapterView) {
                AdapterView listView = (AdapterView) parent;
                this.viewPosition = listView.getFirstVisiblePosition() + this.viewPosition;
            } else if (ClassExistHelper.instanceOfRecyclerView(parent)) {
                int adapterPosition = getChildAdapterPositionInRecyclerView(this.view, parent);
                if (adapterPosition >= 0) {
                    this.viewPosition = adapterPosition;
                }
            }
        }
    }

    private void calculateViewXPath() {
        Object parentObject = this.view.getParent();
        if (parentObject == null || (WindowHelper.get().isDecorView(this.view) && !(parentObject instanceof View))) {
            return;
        }

        StringBuilder indeedXIndex = new StringBuilder(this.indeedXIndex);
        StringBuilder xPath = new StringBuilder(this.xPath);
        StringBuilder xIndex = new StringBuilder(this.xIndex);
        String viewName = ClassUtil.getSimpleClassName(this.view.getClass());

        //针对菜单栏处理
        if (ListMenuItemViewShadow.isListMenuItemView(this.view)) {
            MenuItem menuItem = new ListMenuItemViewShadow(this.view).getMenuItem();
            if (menuItem != null) {
                ViewNodeV4 itemViewNode = generateMenuItemViewNode(this.view.getContext(), menuItem);
                this.xPath = itemViewNode.getXPath();
                this.index = itemViewNode.getIndex();
                this.xIndex = itemViewNode.getXIndex();
                this.viewContent = itemViewNode.getViewContent();
                this.indeedXIndex = itemViewNode.getIndeedXIndex();
                return;
            }
        }

        if (parentObject instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) parentObject;
            if (parent instanceof ExpandableListView) {
                ExpandableListView listParent = (ExpandableListView) parent;
                calculateExpandableListView(listParent, viewName, xPath, indeedXIndex, xIndex);
            } else if (ClassExistHelper.isListView(parent) || ClassExistHelper.instanceOfRecyclerView(parent)) {
                calculateListView(viewName, xPath, indeedXIndex, xIndex);
            } else if (ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(parentObject)
                    || ClassExistHelper.instanceOfSupportSwipeRefreshLayout(parentObject)) {
                xPath.append("/").append(viewName);
                xIndex.append("/0");
                indeedXIndex.append("/0");
            } else {
                calculateDefaultViewGroup(parent, viewName, xPath, indeedXIndex, xIndex);
            }
        } else {
            xPath.append("/").append(viewName);
            xIndex.append("/").append(this.viewPosition);
            indeedXIndex.append("/").append(this.viewPosition);
        }

        String customId = ViewAttributeUtil.getCustomId(this.view);
        String id = ViewAttributeUtil.getViewPackageId(this.view);
        String replaceId = customId == null ? id : customId;

        if (replaceId != null) {
            int lastPath = xIndex.lastIndexOf("/");
            if (lastPath != -1) {
                xIndex.replace(lastPath + 1, xIndex.length(), replaceId);
            }
            lastPath = indeedXIndex.lastIndexOf("/");
            if (lastPath != -1) {
                indeedXIndex.replace(lastPath + 1, indeedXIndex.length(), replaceId);
            }
        }

        this.xPath = xPath.toString();
        this.xIndex = xIndex.toString();
        this.indeedXIndex = indeedXIndex.toString();
    }

    private void calculateViewContent() {
        this.viewContent = ViewAttributeUtil.getViewContent(this.view);
    }

    /**
     * create a new ViewNode after current ViewNode and return the newly created ViewNode.
     */
    public ViewNodeV4 append(View view) {
        if (this.view instanceof ViewGroup) {
            return append(view, ((ViewGroup) (this.view)).indexOfChild(view), false);
        }
        return this;
    }

    public ViewNodeV4 append(View view, int index, boolean ignorePage) {
        boolean hasListParent = isHasListParent() || ClassExistHelper.isListView(view);
        ViewNodeV4 viewNode = new ViewNodeV4();
        viewNode.withView(view)
                .setIndex(hasListParent ? this.index : -1)
                .setXPath(this.xPath)
                .setIndeedXIndex(this.indeedXIndex)
                .setXIndex(this.xIndex)
                .setClickableParentXPath(ViewUtil.canCircle(this.view) ? this.xPath : this.clickableParentXPath)
                .setClickablePatentXIndex(ViewUtil.canCircle(this.view) ? this.xIndex : this.clickablePatentXIndex)
                .setHasListParent(hasListParent)
                .setViewPosition(index)
                .setParent(this)
                .calculate();
        return viewNode;
    }


    public static ViewNodeV4 generateMenuItemViewNode(Context context, MenuItem menuItem) {
        StringBuilder xpath = new StringBuilder();
        StringBuilder xIndex = new StringBuilder();
        String packageId = ViewAttributeUtil.getPackageId(context, menuItem.getItemId());
        xpath.append("/MenuView/MenuItem");
        xIndex.append("/0/").append(packageId == null ? "0" : packageId);

        return new ViewNodeV4()
                .setIndex(-1)
                .setViewContent(menuItem.getTitle() != null ? menuItem.getTitle().toString() : null)
                .setXPath(xpath.toString())
                .setXIndex(xIndex.toString())
                .setIndeedXIndex(xpath.toString());
    }

    private void calculateExpandableListView(ExpandableListView listParent, String viewName, StringBuilder xPath, StringBuilder indeedXIndex, StringBuilder xIndex) {
        long elp = listParent.getExpandableListPosition(this.viewPosition);
        if (ExpandableListView.getPackedPositionType(elp) == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
            if (this.viewPosition < listParent.getHeaderViewsCount()) {
                xPath.append("/ELH/").append(viewName);
                xIndex.append("/").append(this.viewPosition).append("/0");
                indeedXIndex.append("/").append(this.viewPosition).append("/0");
            } else {
                int footerIndex = this.viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                xPath.append("/ELF/").append(viewName);
                xIndex.append("/").append(footerIndex).append("/0");
                indeedXIndex.append("/").append(footerIndex).append("/0");
            }
        } else {
            int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
            int childIdx = ExpandableListView.getPackedPositionChild(elp);
            if (childIdx != -1) {
                this.index = childIdx;
                xPath.append("/ELVG/ELVC/").append(viewName);
                xIndex.delete(0, xIndex.length()).append(indeedXIndex).append("/").append(groupIdx).append("/-").append("/0");
                indeedXIndex.append("/").append(groupIdx).append("/").append(childIdx).append("/0");
            } else {
                this.index = groupIdx;
                xPath.append("/ELVG[").append(groupIdx).append("]/").append(viewName).append("[0]");
                xIndex.delete(0, xIndex.length()).append(indeedXIndex).append("/-").append("/0");
                indeedXIndex.append("/").append(groupIdx).append("/0");
            }
        }
    }

    private void calculateListView(String viewName, StringBuilder xPath, StringBuilder indeedXIndex, StringBuilder xIndex) {
        this.index = this.viewPosition;
        xPath.append("/").append(viewName);
        xIndex.delete(0, xIndex.length()).append(indeedXIndex).append("/-");
        indeedXIndex.append("/-");
    }

    private void calculateDefaultViewGroup(ViewGroup parent, String viewName, StringBuilder xPath, StringBuilder indeedXIndex, StringBuilder xIndex) {
        int matchTypePosition = 0;
        boolean findChildView = false;
        for (int siblingIndex = 0; siblingIndex < parent.getChildCount(); siblingIndex++) {
            View siblingView = parent.getChildAt(siblingIndex);
            if (siblingView == this.view) {
                findChildView = true;
                break;
            } else if (siblingView.getClass().getSimpleName().equals(viewName)) {
                matchTypePosition++;
            }
        }
        if (findChildView) {
            xPath.append("/").append(viewName);
            xIndex.append("/").append(matchTypePosition);
            indeedXIndex.append("/").append(matchTypePosition);
        } else {
            xPath.append("/").append(viewName);
            xIndex.append("/").append(this.viewPosition);
            indeedXIndex.append("/").append(this.viewPosition);
        }
    }

    private int getChildAdapterPositionInRecyclerView(View childView, ViewGroup parentView) {
        if (ClassExistHelper.instanceOfAndroidXRecyclerView(parentView)) {
            return ((androidx.recyclerview.widget.RecyclerView) parentView).getChildAdapterPosition(childView);
        } else if (ClassExistHelper.instanceOfSupportRecyclerView(parentView)) {
            // For low version RecyclerView
            try {
                return ((android.support.v7.widget.RecyclerView) parentView).getChildAdapterPosition(childView);
            } catch (Throwable e) {
                return ((android.support.v7.widget.RecyclerView) parentView).getChildPosition(childView);
            }

        }
        return -1;
    }

    private static final String INPUT = "INPUT";
    private static final String TEXT = "TEXT";
    private static final String WEB_VIEW = "WEB_VIEW";
    private static final String BUTTON = "BUTTON";
    private static final String LIST = "LIST";
    private static final String MENU_ITEM = "MENU_ITEM";

    public String getNodeType() {
        if (ViewUtil.isChangeTypeView(this.view)) {
            return INPUT;
        }
        if (this.view instanceof TextView && !(this.view instanceof Button)) {
            return TEXT;
        }
        if (ClassExistHelper.isListView(this.view.getParent())) {
            return LIST;
        }
        if (ClassExistHelper.isWebView(this.view)) {
            return WEB_VIEW;
        }
        return BUTTON;
    }


    String findPagePath() {
        if (page != null) return page;
        String pagePath = null;
        Page<?> page = ViewAttributeUtil.getViewPage(this.view);
        if (page != null) {
            pagePath = page.path();
        } else {
            pagePath = this.parent.getPage();
        }
        if (pagePath == null) {
            pagePath = PageProvider.get().findPage(this.view).path();
        }
        setPage(pagePath);
        return pagePath;
    }
}
