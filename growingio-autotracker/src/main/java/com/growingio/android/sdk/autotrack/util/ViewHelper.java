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

package com.growingio.android.sdk.autotrack.util;

import android.app.Activity;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;

import androidx.recyclerview.widget.RecyclerView;

import com.growingio.android.sdk.autotrack.events.ViewElement;
import com.growingio.android.sdk.autotrack.events.ViewElementEvent;
import com.growingio.android.sdk.autotrack.events.base.BaseViewElement;
import com.growingio.android.sdk.autotrack.models.ViewNode;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.GIOMainThread;
import com.growingio.android.sdk.track.GInternal;
import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.LinkedString;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ViewHelper {

    private final static String TAG = "GIO.ViewHelper";

    private ViewHelper() {
    }

    public static boolean isViewSelfVisible(View mView) {
        if (mView == null || mView.getWindowVisibility() == View.GONE) {
            return false;
        }

        // home键back后, DecorView的visibility是 INVISIBLE, 即onResume时Window并不可见, 对GIO而言此时是可见的
        if (WindowHelper.isDecorView(mView)) {
            return true;
        }

        if (!(mView.getWidth() > 0
                && mView.getHeight() > 0
                && mView.getAlpha() > 0
                && mView.getLocalVisibleRect(new Rect()))) {
            return false;
        }

        //动画导致用户可见但是仍然 invisible,
        if (mView.getVisibility() != View.VISIBLE
                && mView.getAnimation() != null
                && mView.getAnimation().getFillAfter()) {
            return true;
        } else {
            return mView.getVisibility() == View.VISIBLE;
        }

    }

    public static boolean viewVisibilityInParents(View view) {
        if (view == null) {
            return false;
        }

        if (!isViewSelfVisible(view)) {
            return false;
        }

        ViewParent viewParent = view.getParent();
        while (viewParent instanceof View) {
            if (isViewSelfVisible((View) viewParent)) {
                viewParent = viewParent.getParent();
                if (viewParent == null) {
                    LogUtil.d(TAG, "Hit detached view: ", viewParent);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static ViewNode getClickViewNode(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }
        WindowHelper.init();
        View[] windows = WindowHelper.getWindowViews();
        try {
            for (View window : windows) {
                if (window.getClass() == WindowHelper.sPopupWindowClazz) {
                    View menuView = findMenuItemView(window, menuItem);
                    if (menuView != null) {
                        return getClickViewNode(menuView);
                    }
                }
            }
            for (View window : windows) {
                if (window.getClass() != WindowHelper.sPopupWindowClazz) {
                    View menuView = findMenuItemView(window, menuItem);
                    if (menuView != null) {
                        return getClickViewNode(menuView);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ViewNode getClickViewNode(View view) {
        if (!GConfig.getInstance().isEnabled() || view == null) {
            return null;
        }
        Activity activity = ActivityStateProvider.ActivityStatePolicy.get().getForegroundActivity();
        if (activity == null || Util.isIgnoredView(view)) {
            return null;
        }

        ViewNode viewNode = getViewNode(view);

        if (viewNode == null) {
            return null;
        }

        return viewNode;
    }

    public static ViewElementEvent.EventBuilder getClickActionEvent(ViewNode viewNode) {
        if (viewNode == null || viewNode.view == null) {
            return null;
        }

        if (!GConfig.getInstance().isEnabled()) {
            return null;
        }
        Activity activity = ActivityStateProvider.ActivityStatePolicy.get().getForegroundActivity();
        if (activity == null || Util.isIgnoredView(viewNode.view)) {
            return null;
        }

        ViewElementEvent.EventBuilder event = new ViewElementEvent.EventBuilder(GInternal.getInstance().getMainThread().getCoreAppState());
        event.setEventType(EventType.CLICK);
        Page<?> page = PageProvider.PagePolicy.get().findPage(viewNode.view);
        event.addElementBuilders(viewNodeToElementBuilders(viewNode))
                .setPageName(page.path())
                .setPageShowTimestamp(page.getShowTimestamp());

        return event;
    }

    public static ViewNode getViewNode(View view) {
        ArrayList<View> viewTreeList = new ArrayList<View>(8);
        ViewParent parent = view.getParent();
        viewTreeList.add(view);
        /*
         * view:
         * "AppCompactButton[2]#login_btn
         * parents:
         * ["LinearLayout[3]#login_container" ,"RelativeLayout[1]", ,"FrameLayout[0]#content", "PhoneWindow$DecorView"]
         */
        Page<?> page = ViewAttributeUtil.getViewPage(view);
        boolean needTraverse = (page == null || page.isIgnored()) && ViewAttributeUtil.getCustomId(view) == null;
        if (needTraverse) {
            while (parent instanceof ViewGroup) {
                if (Util.isIgnoredView((View) parent)) {
                    return null;
                }

                viewTreeList.add((ViewGroup) parent);
                if (ViewAttributeUtil.getCustomId((View)parent) != null) {
                    break;
                }
                page = ViewAttributeUtil.getViewPage((View) parent);
                if (page != null && !page.isIgnored()) {
                    break;
                }
                parent = parent.getParent();
            }
        }

        int endIndex = viewTreeList.size() - 1;
        View rootView = viewTreeList.get(endIndex);
        WindowHelper.init();

        String bannerText = null;

        int viewPosition = 0;
        int listPos = -1;
        boolean mHasListParent = false;
        boolean mParentIdSettled = false;
        String prefix = WindowHelper.getWindowPrefix(rootView);
        String opx = prefix;
        String px = prefix;

        if (!WindowHelper.isDecorView(rootView) && !(rootView.getParent() instanceof View)) {
            opx += "/" + Util.getSimpleClassName(rootView.getClass());
            px = opx;
            if (GConfig.getInstance().useID()) {
                String id = Util.getIdName(rootView, mParentIdSettled);
                if (id != null) {
                    if (ViewAttributeUtil.getViewId(rootView) != null) {
                        mParentIdSettled = true;
                    }
                    opx += "#" + id;
                    px += "#" + id;
                }
            }
        } else if (ViewAttributeUtil.getCustomId(rootView) != null) {
            opx = "/" + ViewAttributeUtil.getCustomId(rootView);
            px = opx;
        }
        if (rootView instanceof ViewGroup) {
            ViewGroup parentView = (ViewGroup) rootView;
            for (int i = endIndex - 1; i >= 0; i--) {
                viewPosition = 0;
                View childView = viewTreeList.get(i);
                String viewName = ViewAttributeUtil.getViewNameKey(childView);
                if (viewName != null) {
                    opx += "/" + viewName;
                    px += "/" + viewName;
                } else {
                    viewName = Util.getSimpleClassName(childView.getClass());
                    viewPosition = parentView.indexOfChild(childView);
                    if (ClassExistHelper.instanceOfAndroidXViewPager(parentView)) {
                        viewPosition = ((androidx.viewpager.widget.ViewPager) parentView).getCurrentItem();
                        mHasListParent = true;
                    } else if (ClassExistHelper.instanceOfSupportViewPager(parentView)) {
                        viewPosition = ((ViewPager) parentView).getCurrentItem();
                        mHasListParent = true;
                    } else if (parentView instanceof AdapterView) {
                        AdapterView listView = (AdapterView) parentView;
                        viewPosition = listView.getFirstVisiblePosition() + viewPosition;
                        mHasListParent = true;
                    } else if (ClassExistHelper.instanceOfRecyclerView(parentView)) {
                        int adapterPosition = getChildAdapterPositionInRecyclerView(childView, parentView);
                        if (adapterPosition >= 0) {
                            mHasListParent = true;
                            viewPosition = adapterPosition;
                        }
                    }
                    if (parentView instanceof ExpandableListView) {
                        ExpandableListView listParent = (ExpandableListView) parentView;
                        long elp = listParent.getExpandableListPosition(viewPosition);
                        if (ExpandableListView.getPackedPositionType(elp) == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
                            if (viewPosition < listParent.getHeaderViewsCount()) {
                                opx = opx + "/ELH[" + viewPosition + "]/" + viewName + "[0]";
                                px = px + "/ELH[" + viewPosition + "]/" + viewName + "[0]";
                            } else {
                                int footerIndex = viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                                opx = opx + "/ELF[" + footerIndex + "]/" + viewName + "[0]";
                                px = px + "/ELF[" + footerIndex + "]/" + viewName + "[0]";
                            }
                        } else {
                            int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
                            int childIdx = ExpandableListView.getPackedPositionChild(elp);
                            if (childIdx != -1) {
                                listPos = childIdx;
                                px = opx + "/ELVG[" + groupIdx + "]/ELVC[-]/" + viewName + "[0]";
                                opx = opx + "/ELVG[" + groupIdx + "]/ELVC[" + childIdx + "]/" + viewName + "[0]";
                            } else {
                                listPos = groupIdx;
                                px = opx + "/ELVG[-]/" + viewName + "[0]";
                                opx = opx + "/ELVG[" + groupIdx + "]/" + viewName + "[0]";
                            }
                        }
                    } else if (Util.isListView(parentView) || ClassExistHelper.instanceOfRecyclerView(parentView)) {
                        // 处理有特殊的position的元素
                        List bannerTag = ViewAttributeUtil.getBannerKey(parentView);
                        if (bannerTag != null && (bannerTag).size() > 0) {
                            viewPosition = Util.calcBannerItemPosition(bannerTag, viewPosition);
                            bannerText = Util.truncateViewContent(String.valueOf(bannerTag.get(viewPosition)));
                        }
                        listPos = viewPosition;
                        px = opx + "/" + viewName + "[-]";
                        opx = opx + "/" + viewName + "[" + listPos + "]";
                    } else if (ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(parentView)
                            || ClassExistHelper.instanceOfSupportSwipeRefreshLayout(parentView)) {
                        opx = opx + "/" + viewName + "[0]";
                        px = px + "/" + viewName + "[0]";
                    } else {
                        int matchTypePostion = 0;
                        String matchType = childView.getClass().getSimpleName();
                        boolean findChildView = false;
                        for (int siblingIndex = 0; siblingIndex < parentView.getChildCount(); siblingIndex++) {
                            View siblingView = parentView.getChildAt(siblingIndex);
                            if (siblingView == childView) {
                                findChildView = true;
                                break;
                            } else if (siblingView.getClass().getSimpleName().equals(matchType)) {
                                matchTypePostion++;
                            }
                        }
                        if (findChildView) {
                            opx = opx + "/" + viewName + "[" + matchTypePostion + "]";
                            px = px + "/" + viewName + "[" + matchTypePostion + "]";
                        } else {
                            opx = opx + "/" + viewName + "[" + viewPosition + "]";
                            px = px + "/" + viewName + "[" + viewPosition + "]";
                        }
                    }
                    if (GConfig.getInstance().useID()) {
                        String id = Util.getIdName(childView, mParentIdSettled);
                        if (id != null) {
                            if (ViewAttributeUtil.getViewId(childView) != null) {
                                mParentIdSettled = true;
                            }
                            opx += "#" + id;
                            px += "#" + id;
                        }
                    }
                }

                if (childView instanceof ViewGroup) {
                    parentView = (ViewGroup) childView;
                } else {
                    break;
                }
            }
        }

        ViewNode viewNode = new ViewNode(view, listPos, mHasListParent, prefix.equals(WindowHelper.getMainWindowPrefix()), true, mParentIdSettled,
                LinkedString.fromString(opx),
                LinkedString.fromString(px), prefix);
        viewNode.viewContent = Util.getViewContent(view, bannerText);
        viewNode.clickableParentXPath = LinkedString.fromString(px);
        viewNode.bannerText = bannerText;
        viewNode.cid = ViewAttributeUtil.getCustomId(view);


        return viewNode;
    }

    public static int getChildAdapterPositionInRecyclerView(View childView, ViewGroup parentView) {
        if (ClassExistHelper.instanceOfAndroidXRecyclerView(parentView)) {
            return ((androidx.recyclerview.widget.RecyclerView) parentView).getChildAdapterPosition(childView);
        } else if (ClassExistHelper.instanceOfSupportRecyclerView(parentView)) {
            // For low version RecyclerView
            try {
                return ((RecyclerView) parentView).getChildAdapterPosition(childView);
            } catch (Throwable e) {
                return ((RecyclerView) parentView).getChildPosition(childView);
            }

        } else if (ClassExistHelper.sHasCustomRecyclerView) {
            return ClassExistHelper.invokeCRVGetChildAdapterPositionMethod(parentView, childView);
        }
        return -1;
    }

    public static void persistClickEvent(BaseEvent.BaseEventBuilder<?> click) {
        GIOMainThread mainThread = GInternal.getInstance().getMainThread();
        if (mainThread != null) {
            mainThread.postEventToGMain(click);
        }
    }

    private static View findMenuItemView(View view, MenuItem item) throws InvocationTargetException, IllegalAccessException {
        if (WindowHelper.getMenuItemData(view) == item) {
            return view;
        } else if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View menuView = findMenuItemView(((ViewGroup) view).getChildAt(i), item);
                if (menuView != null) {
                    return menuView;
                }
            }
        }
        return null;
    }

    private static boolean shouldChangeOn(View view, ViewNode viewNode) {
        if (view instanceof EditText) {
            String tag = ViewAttributeUtil.getMonitoringFocusKey(view);
            String lastText = tag == null ? "" : tag;
            String nowText = ((EditText) view).getText().toString();
            if ((TextUtils.isEmpty(nowText) && TextUtils.isEmpty(lastText)) || lastText.equals(nowText)) {
                return false;
            }
            ViewAttributeUtil.setMonitoringFocusKey(view, nowText);
            return true;
        }
        return false;
    }

    public static void changeOn(View view) {
        if (!GConfig.getInstance().isInitSucceeded() || !GConfig.getInstance().isEnabled()) {
            return;
        }
        Activity activity = ActivityUtil.findActivity(view.getContext());
        if (activity == null || Util.isIgnoredView(view)) {
            return;
        }

        ViewNode viewNode = getViewNode(view);

        if (viewNode == null) {
            return;
        }

        if (!shouldChangeOn(view, viewNode)) {
            return;
        }

        GIOMainThread mainThread = GInternal.getInstance().getMainThread();
        if (mainThread != null) {
            ViewElementEvent.EventBuilder event = new ViewElementEvent.EventBuilder(mainThread.getCoreAppState());
            Page<?> page = PageProvider.PagePolicy.get().findPage(viewNode.view);
            event.setEventType(EventType.CHANGE)
                    .addElementBuilders(viewNodeToElementBuilders(viewNode))
                    .setPageName(page.path())
                    .setPageShowTimestamp(page.getShowTimestamp());
            mainThread.postEventToGMain(event);
        }
    }

    private static List<BaseViewElement.BaseElementBuilder<?>> viewNodeToElementBuilders(ViewNode viewNode) {
        List<BaseViewElement.BaseElementBuilder<?>> mElementBuilders = new ArrayList<BaseViewElement.BaseElementBuilder<?>>();
        mElementBuilders.add((new ViewElement.ElementBuilder())
                .setCid(viewNode.cid)
                .setXpath(viewNode.parentXPath.toStringValue())
                .setTimestamp(System.currentTimeMillis())
                .setIndex(viewNode.lastListPos)
                .setTextValue(viewNode.viewContent)
        );
        return mElementBuilders;
    }
}
