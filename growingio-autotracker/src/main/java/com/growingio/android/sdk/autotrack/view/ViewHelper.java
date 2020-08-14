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

package com.growingio.android.sdk.autotrack.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsSeekBar;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.models.ViewNode;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ViewHelper {
    private final static String TAG = "ViewHelper";

    public static final String PAGE_PREFIX = "/Page";

    private static final int MAX_CONTENT_LENGTH = 100;
    private static final int PACKAGE_ID_START = 0x7f000000;

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

    @Nullable
    public static ViewNode getClickViewNode(View view) {
        if (view == null) {
            return null;
        }
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null || ViewHelper.isIgnoredView(view)) {
            return null;
        }

        return getViewNode(view);
    }

    @Nullable
    public static String getViewPackageId(View view) {
        try {
            int viewId = view.getId();
            if (viewId <= PACKAGE_ID_START) {
                return null;
            }
            return view.getContext().getResources().getResourceEntryName(viewId);
        } catch (Resources.NotFoundException e) {
            LogUtil.e(TAG, e);
        }
        return null;
    }

    public static ViewNode getViewNode(View view) {
        ArrayList<View> viewTreeList = new ArrayList<>(8);
        ViewParent parent = view.getParent();
        viewTreeList.add(view);
        Page<?> page = ViewAttributeUtil.getViewPage(view);
        boolean needTraverse = (page == null || page.isIgnored()) && ViewAttributeUtil.getCustomId(view) == null;
        if (needTraverse) {
            while (parent instanceof ViewGroup) {
                viewTreeList.add((View) parent);
                if (ViewAttributeUtil.getCustomId((View) parent) != null) {
                    break;
                }
                page = ViewAttributeUtil.getViewPage((View) parent);
                if (page != null && !page.isIgnored()) {
                    break;
                }
                parent = parent.getParent();
            }
        }

        View rootView = viewTreeList.get(viewTreeList.size() - 1);
        WindowHelper.init();

        int viewPosition;
        int listPos = -1;
        StringBuilder xpath;
        StringBuilder originalXpath;

        if (ViewAttributeUtil.getCustomId(rootView) != null) {
            originalXpath = new StringBuilder("/" + ViewAttributeUtil.getCustomId(rootView));
            xpath = new StringBuilder(originalXpath.toString());
            viewTreeList.remove(viewTreeList.size() - 1);
        } else if (ViewAttributeUtil.getViewPage(rootView) != null) {
            originalXpath = new StringBuilder(PAGE_PREFIX);
            xpath = new StringBuilder(originalXpath.toString());
            viewTreeList.remove(viewTreeList.size() - 1);
        } else {
            String prefix = WindowHelper.getWindowPrefix(rootView);
            xpath = new StringBuilder(prefix);
            originalXpath = new StringBuilder(prefix);
        }

        if (rootView instanceof ViewGroup) {
            ViewGroup parentView = (ViewGroup) rootView;
            for (int i = viewTreeList.size() - 1; i >= 0; i--) {
                View childView = viewTreeList.get(i);
                String viewName = ClassUtil.getSimpleClassName(childView.getClass());
                viewPosition = parentView.indexOfChild(childView);
                if (ClassExistHelper.instanceOfAndroidXViewPager(parentView)) {
                    viewPosition = ((androidx.viewpager.widget.ViewPager) parentView).getCurrentItem();
                } else if (ClassExistHelper.instanceOfSupportViewPager(parentView)) {
                    viewPosition = ((ViewPager) parentView).getCurrentItem();
                } else if (parentView instanceof AdapterView) {
                    AdapterView<?> listView = (AdapterView<?>) parentView;
                    viewPosition = listView.getFirstVisiblePosition() + viewPosition;
                } else if (ClassExistHelper.instanceOfRecyclerView(parentView)) {
                    int adapterPosition = getChildAdapterPositionInRecyclerView(childView, parentView);
                    if (adapterPosition >= 0) {
                        viewPosition = adapterPosition;
                    }
                }
                if (parentView instanceof ExpandableListView) {
                    ExpandableListView listParent = (ExpandableListView) parentView;
                    long elp = listParent.getExpandableListPosition(viewPosition);
                    if (ExpandableListView.getPackedPositionType(elp) == ExpandableListView.PACKED_POSITION_TYPE_NULL) {
                        if (viewPosition < listParent.getHeaderViewsCount()) {
                            originalXpath.append("/ELH[").append(viewPosition).append("]/").append(viewName).append("[0]");
                            xpath.append("/ELH[").append(viewPosition).append("]/").append(viewName).append("[0]");
                        } else {
                            int footerIndex = viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                            originalXpath.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                            xpath.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                        }
                    } else {
                        int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
                        int childIdx = ExpandableListView.getPackedPositionChild(elp);
                        if (childIdx != -1) {
                            listPos = childIdx;
                            xpath = new StringBuilder(originalXpath + "/ELVG[" + groupIdx + "]/ELVC[-]/" + viewName + "[0]");
                            originalXpath.append("/ELVG[").append(groupIdx).append("]/ELVC[").append(childIdx).append("]/").append(viewName).append("[0]");
                        } else {
                            listPos = groupIdx;
                            xpath = new StringBuilder(originalXpath + "/ELVG[-]/" + viewName + "[0]");
                            originalXpath.append("/ELVG[").append(groupIdx).append("]/").append(viewName).append("[0]");
                        }
                    }
                } else if (ClassExistHelper.isListView(parentView)) {
                    listPos = viewPosition;
                    xpath = new StringBuilder(originalXpath + "/" + viewName + "[-]");
                    originalXpath.append("/").append(viewName).append("[").append(listPos).append("]");
                } else if (ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(parentView)
                        || ClassExistHelper.instanceOfSupportSwipeRefreshLayout(parentView)) {
                    originalXpath.append("/").append(viewName).append("[0]");
                    xpath.append("/").append(viewName).append("[0]");
                } else {
                    int matchTypePosition = 0;
                    String matchType = ClassUtil.getSimpleClassName(childView.getClass());
                    boolean findChildView = false;
                    for (int siblingIndex = 0; siblingIndex < parentView.getChildCount(); siblingIndex++) {
                        View siblingView = parentView.getChildAt(siblingIndex);
                        if (siblingView == childView) {
                            findChildView = true;
                            break;
                        } else if (siblingView.getClass().getSimpleName().equals(matchType)) {
                            matchTypePosition++;
                        }
                    }
                    if (findChildView) {
                        originalXpath.append("/").append(viewName).append("[").append(matchTypePosition).append("]");
                        xpath.append("/").append(viewName).append("[").append(matchTypePosition).append("]");
                    } else {
                        originalXpath.append("/").append(viewName).append("[").append(viewPosition).append("]");
                        xpath.append("/").append(viewName).append("[").append(viewPosition).append("]");
                    }
                }
                String id = getViewPackageId(childView);
                if (id != null) {
                    originalXpath.append("#").append(id);
                    xpath.append("#").append(id);
                }

                if (childView instanceof ViewGroup) {
                    parentView = (ViewGroup) childView;
                } else {
                    break;
                }
            }
        }

        String viewContent = getViewContent(view);
        return new ViewNode(view, xpath.toString(), originalXpath.toString(), viewContent, listPos);
    }

    private static int getChildAdapterPositionInRecyclerView(View childView, ViewGroup parentView) {
        if (ClassExistHelper.instanceOfAndroidXRecyclerView(parentView)) {
            return ((androidx.recyclerview.widget.RecyclerView) parentView).getChildAdapterPosition(childView);
        } else if (ClassExistHelper.instanceOfSupportRecyclerView(parentView)) {
            // For low version RecyclerView
            try {
                return ((RecyclerView) parentView).getChildAdapterPosition(childView);
            } catch (Throwable e) {
                return ((RecyclerView) parentView).getChildPosition(childView);
            }

        }
        return -1;
    }

    public static void persistClickEvent(BaseEvent.BaseEventBuilder<?> click) {
        TrackMainThread.trackMain().postEventToTrackMain(click);
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
        if (!GrowingTracker.initializedSuccessfully()) {
            return;
        }
        Activity activity = ActivityUtil.findActivity(view.getContext());
        if (activity == null || ViewHelper.isIgnoredView(view)) {
            return;
        }

        ViewNode viewNode = getViewNode(view);

        if (viewNode == null) {
            return;
        }

        if (!shouldChangeOn(view, viewNode)) {
            return;
        }

//        ViewElementEvent.EventBuilder event = new ViewElementEvent.EventBuilder();
//        Page<?> page = PageProvider.get().findPage(viewNode.mView);
//        event.setEventType(EventType.CHANGE)
//                .addElementBuilders(viewNodeToElementBuilders(viewNode))
//                .setPageName(page.path())
//                .setPageShowTimestamp(page.getShowTimestamp());
//        TrackMainThread.trackMain().postEventToTrackMain(event);
    }

    public static boolean isIgnoredView(View view) {
        IgnorePolicy ignorePolicy = ViewAttributeUtil.getIgnorePolicy(view);
        if (ignorePolicy != null) {
            return ignorePolicy == IgnorePolicy.IGNORE_SELF || ignorePolicy == IgnorePolicy.IGNORE_ALL;
        }
        return isIgnoredByParent(view);
    }

    private static boolean isIgnoredByParent(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            IgnorePolicy ignorePolicy = ViewAttributeUtil.getIgnorePolicy((View) parent);
            if ((ignorePolicy == IgnorePolicy.IGNORE_ALL || ignorePolicy == IgnorePolicy.IGNORE_CHILD)) {
                return true;
            }
            return isIgnoredByParent((View) parent);
        }
        return false;
    }

    public static String getViewContent(View view) {
        String value = "";
        String contentTag = ViewAttributeUtil.getContent(view);
        if (contentTag != null) {
            value = contentTag;
        } else {
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                if (ViewAttributeUtil.getTrackText(editText) != null) {
                    if (!isPasswordInputType(editText.getInputType())) {
                        CharSequence sequence = editText.getText();
                        value = sequence == null ? "" : sequence.toString();
                    }
                }
            } else if (view instanceof RatingBar) {
                value = String.valueOf(((RatingBar) view).getRating());
            } else if (view instanceof Spinner) {
                Object item = ((Spinner) view).getSelectedItem();
                if (item instanceof String) {
                    value = (String) item;
                } else {
                    View selected = ((Spinner) view).getSelectedView();
                    if (selected instanceof TextView && ((TextView) selected).getText() != null) {
                        value = ((TextView) selected).getText().toString();
                    }
                }
            } else if (view instanceof SeekBar) {
                value = String.valueOf(((SeekBar) view).getProgress());
            } else if (view instanceof RadioGroup) {
                RadioGroup group = (RadioGroup) view;
                View selected = group.findViewById(group.getCheckedRadioButtonId());
                if (selected instanceof RadioButton && ((RadioButton) selected).getText() != null) {
                    value = ((RadioButton) selected).getText().toString();
                }
            } else if (view instanceof TextView) {
                if (((TextView) view).getText() != null) {
                    value = ((TextView) view).getText().toString();
                }
            }

            if (TextUtils.isEmpty(value)) {
                if (view.getContentDescription() != null) {
                    value = view.getContentDescription().toString();
                }
            }
        }
        return truncateViewContent(value);
    }

    public static String truncateViewContent(String value) {
        if (value == null) {
            return "";
        }
        if (!TextUtils.isEmpty(value)) {
            if (value.length() > MAX_CONTENT_LENGTH) {
                value = value.substring(0, MAX_CONTENT_LENGTH);
            }
        }
        return value;
    }


    public static boolean isClickableView(View view) {
        return view.isClickable() || view instanceof RadioGroup || view instanceof Spinner || view instanceof AbsSeekBar
                || (view.getParent() != null && view.getParent() instanceof AdapterView
                && ((AdapterView) view.getParent()).isClickable());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static boolean isPasswordInputType(int inputType) {
        final int variation = inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }
}