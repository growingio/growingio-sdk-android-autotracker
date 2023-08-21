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
 * @author cpacm 2023/7/10
 */
class ViewNodeV3 {
    private View view;
    private String xPath;
    private String originalXPath;
    private String clickableParentXPath;
    private String viewContent;
    private boolean hasListParent;
    private String prefixPage;
    private int index;

    private String page;

    private int viewPosition; //for xpath calculate

    private ViewNodeV3 parent;

    ViewNodeV3 withView(View view) {
        this.view = view;
        return this;
    }

    View getView() {
        return view;
    }

    ViewNodeV3 setXPath(String xPath) {
        this.xPath = xPath;
        return this;
    }

    String getXPath() {
        return xPath;
    }

    ViewNodeV3 setOriginalXPath(String originalXPath) {
        this.originalXPath = originalXPath;
        return this;
    }

    String getOriginalXPath() {
        return originalXPath;
    }

    ViewNodeV3 setClickableParentXPath(String clickableParentXPath) {
        this.clickableParentXPath = clickableParentXPath;
        return this;
    }

    String getClickableParentXPath() {
        return clickableParentXPath;
    }

    ViewNodeV3 setViewContent(String viewContent) {
        this.viewContent = viewContent;
        return this;
    }

    String getViewContent() {
        return viewContent;
    }

    ViewNodeV3 setHasListParent(boolean hasListParent) {
        this.hasListParent = hasListParent;
        return this;
    }

    boolean isHasListParent() {
        return hasListParent;
    }

    ViewNodeV3 setPrefixPage(String prefixPage) {
        this.prefixPage = prefixPage;
        return this;
    }

    String getPrefixPage() {
        return prefixPage;
    }

    ViewNodeV3 setIndex(int index) {
        this.index = index;
        return this;
    }

    int getIndex() {
        return index;
    }

    public ViewNodeV3 setViewPosition(int viewPosition) {
        this.viewPosition = viewPosition;
        return this;
    }

    public ViewNodeV3 getParent() {
        return parent;
    }

    public ViewNodeV3 setParent(ViewNodeV3 parent) {
        this.parent = parent;
        return this;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void calculate(boolean ignorePage) {
        calculateViewPosition();
        calculateViewXPath(ignorePage);
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

    private void calculateViewXPath(boolean ignorePage) {
        Object parentObject = this.view.getParent();
        if (parentObject == null || (WindowHelper.get().isDecorView(this.view) && !(parentObject instanceof View))) {
            return;
        }

        String customId = ViewAttributeUtil.getCustomId(this.view);
        Page<?> page = ViewAttributeUtil.getViewPage(this.view);
        if (customId != null) {
            this.originalXPath = "/" + customId;
            this.xPath = originalXPath;
            return;
        } else if (page != null) {
            // 圈选为bfs，需要考虑page是否被忽略
            // 点击为dfs，计算时所有page均为忽略
            if (ignorePage) {
                this.originalXPath = "/Page";
            } else {
                this.originalXPath = this.prefixPage + "/" + page.getName();
                String tag = page.getTag();
                if (tag != null) {
                    tag = tag.isEmpty() ? "-" : tag;
                    this.originalXPath += "[" + tag + "]";
                }
            }
            this.xPath = this.originalXPath;
            this.prefixPage = this.originalXPath;
        }

        StringBuilder originalXPath = new StringBuilder().append(this.originalXPath != null ? this.originalXPath : "");
        StringBuilder xPath = new StringBuilder().append(this.xPath != null ? this.xPath : "");
        String viewName = ClassUtil.getSimpleClassName(this.view.getClass());

        //针对菜单栏处理
        if (ListMenuItemViewShadow.isListMenuItemView(this.view)) {
            MenuItem menuItem = new ListMenuItemViewShadow(this.view).getMenuItem();
            if (menuItem != null) {
                ViewNodeV3 itemViewNode = generateMenuItemViewNode(this.view.getContext(), menuItem);
                this.xPath = itemViewNode.getXPath();
                this.index = itemViewNode.getIndex();
                this.viewContent = itemViewNode.getViewContent();
                this.originalXPath = itemViewNode.getOriginalXPath();
                this.prefixPage = itemViewNode.getOriginalXPath();
                return;
            }
        }

        if (parentObject instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) parentObject;
            if (parent instanceof ExpandableListView) {
                ExpandableListView listParent = (ExpandableListView) parent;
                calculateExpandableListView(listParent, viewName, originalXPath, xPath);
            } else if (ClassExistHelper.isListView(parent) || ClassExistHelper.instanceOfRecyclerView(parent)) {
                calculateListView(viewName, originalXPath, xPath);
            } else if (ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(parentObject)
                    || ClassExistHelper.instanceOfSupportSwipeRefreshLayout(parentObject)) {
                originalXPath.append("/").append(viewName).append("[0]");
                xPath.append("/").append(viewName).append("[0]");
            } else {
                calculateDefaultViewGroup(parent, viewName, originalXPath, xPath);
            }
        } else {
            originalXPath.append("/").append(viewName).append("[").append(this.viewPosition).append("]");
            xPath.append("/").append(viewName).append("[").append(this.viewPosition).append("]");
        }

        String id = ViewAttributeUtil.getViewPackageId(this.view);
        if (id != null) {
            originalXPath.append("#").append(id);
            xPath.append("#").append(id);
        }

        this.xPath = xPath.toString();
        this.originalXPath = originalXPath.toString();
    }

    private void calculateViewContent() {
        this.viewContent = ViewAttributeUtil.getViewContent(this.view);
    }

    /**
     * create a new ViewNode after current ViewNode and return the newly created ViewNode.
     */
    public ViewNodeV3 append(View view) {
        if (this.view instanceof ViewGroup) {
            return append(view, ((ViewGroup) (this.view)).indexOfChild(view), false);
        }
        return this;
    }

    public ViewNodeV3 append(View view, int index, boolean ignorePage) {
        boolean hasListParent = isHasListParent() || ClassExistHelper.isListView(view);
        ViewNodeV3 viewNode = new ViewNodeV3();
        viewNode.withView(view)
                .setIndex(hasListParent ? this.index : -1)
                .setXPath(this.xPath)
                .setOriginalXPath(this.originalXPath)
                .setClickableParentXPath(ViewUtil.canCircle(this.view) ? this.xPath : this.clickableParentXPath)
                .setHasListParent(hasListParent)
                .setPrefixPage(this.prefixPage)
                .setViewPosition(index)
                .setParent(this)
                .calculate(ignorePage);
        return viewNode;
    }

    public static ViewNodeV3 generateMenuItemViewNode(Context context, MenuItem menuItem) {
        StringBuilder xpath = new StringBuilder();
        xpath.append("/").append(PageHelper.PAGE_PREFIX);
        xpath.append("/MenuView/MenuItem#").append(ViewAttributeUtil.getPackageId(context, menuItem.getItemId()));

        return new ViewNodeV3()
                .setIndex(-1)
                .setViewContent(menuItem.getTitle() != null ? menuItem.getTitle().toString() : null)
                .setXPath(xpath.toString())
                .setOriginalXPath(xpath.toString())
                .setPrefixPage(xpath.toString());
    }

    private void calculateExpandableListView(ExpandableListView listParent, String viewName, StringBuilder originalXPath, StringBuilder xPath) {
        long elp = listParent.getExpandableListPosition(this.viewPosition);
        if (ExpandableListView.getPackedPositionType(elp) == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
            if (this.viewPosition < listParent.getHeaderViewsCount()) {
                originalXPath.append("/ELH[").append(this.viewPosition).append("]/").append(viewName).append("[0]");
                xPath.append("/ELH[").append(this.viewPosition).append("]/").append(viewName).append("[0]");
            } else {
                int footerIndex = this.viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                originalXPath.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                xPath.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
            }
        } else {
            int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
            int childIdx = ExpandableListView.getPackedPositionChild(elp);
            if (childIdx != -1) {
                this.index = childIdx;
                xPath.delete(0, xPath.length()).append(originalXPath).append("/ELVG[").append(groupIdx).append("]/ELVC[-]/").append(viewName).append("[0]");
                originalXPath.append("/ELVG[").append(groupIdx).append("]/ELVC[").append(childIdx).append("]/").append(viewName).append("[0]");
            } else {
                this.index = groupIdx;
                xPath.delete(0, xPath.length()).append(originalXPath).append("/ELVG[-]/").append(viewName).append("[0]");
                originalXPath.append("/ELVG[").append(groupIdx).append("]/").append(viewName).append("[0]");
            }
        }
    }

    private void calculateListView(String viewName, StringBuilder originalXPath, StringBuilder xPath) {
        this.index = this.viewPosition;
        xPath.delete(0, xPath.length()).append(originalXPath).append("/").append(viewName).append("[-]");
        originalXPath.append("/").append(viewName).append("[").append(this.viewPosition).append("]");
    }

    private void calculateDefaultViewGroup(ViewGroup parent, String viewName, StringBuilder originalXPath, StringBuilder xPath) {
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
            originalXPath.append("/").append(viewName).append("[").append(matchTypePosition).append("]");
            xPath.append("/").append(viewName).append("[").append(matchTypePosition).append("]");
        } else {
            originalXPath.append("/").append(viewName).append("[").append(this.viewPosition).append("]");
            xPath.append("/").append(viewName).append("[").append(this.viewPosition).append("]");
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
