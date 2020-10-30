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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.shadow.ListMenuItemViewShadow;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

import java.util.ArrayList;
import java.util.List;

public class ViewHelper {
    private final static String TAG = "ViewHelper";

    private static final int MAX_CONTENT_LENGTH = 100;
    private static final int PACKAGE_ID_START = 0x7f000000;

    private static final String POPUP_DECOR_VIEW_CLASS_NAME = "PopupDecorView";

    private ViewHelper() {
    }

    public static boolean isViewSelfVisible(View mView) {
        if (mView == null || mView.getWindowVisibility() == View.GONE) {
            return false;
        }

        // home键back后, DecorView的visibility是 INVISIBLE, 即onResume时Window并不可见, 对GIO而言此时是可见的
        if (WindowHelper.get().isDecorView(mView)) {
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
                    Logger.d(TAG, "Hit detached view: ", viewParent);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static ViewNode getMenuItemViewNode(Page<?> page, MenuItem menuItem) {
        StringBuilder xpath = new StringBuilder();
        if (page.isIgnored()) {
            xpath.append(WindowHelper.IGNORE_PAGE_PREFIX);
        } else {
            xpath.append(WindowHelper.PAGE_PREFIX);
        }
        Context context = ContextProvider.getApplicationContext();
        xpath.append("/MenuView/MenuItem#").append(getPackageId(context, menuItem.getItemId()));

        return ViewNode.ViewNodeBuilder.newViewNode()
                .needRecalculate(false)
                .setIndex(-1)
                .setViewContent(menuItem.getTitle().toString())
                .setXPath(xpath.toString())
                .setOriginalXPath(xpath.toString())
                .setPrefixPage(xpath.toString())
                .build();
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
        return getPackageId(view.getContext(), view.getId());
    }


    public static String getPackageId(Context context, int id) {
        try {
            if (id <= PACKAGE_ID_START) {
                return null;
            }
            return context.getResources().getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    private static ViewNode getViewNode(View view) {
        if (ListMenuItemViewShadow.isListMenuItemView(view)) {
            MenuItem menuItem = new ListMenuItemViewShadow(view).getMenuItem();
            if (menuItem != null) {
                Activity activity = ActivityStateProvider.get().getForegroundActivity();
                Page<?> page = PageProvider.get().findPage(activity);
                return getMenuItemViewNode(page, menuItem);
            }
        }
        ArrayList<View> viewTreeList = new ArrayList<>(8);
        ViewNode viewNode = getTopViewNode(view, viewTreeList);

        for (int i = viewTreeList.size() - 2; i >= 0; i--) {
            viewNode = viewNode.appendNode(viewTreeList.get(i));
        }

        return viewNode;
    }

    public static ViewNode getTopViewNode(View view, List<View> viewTreeList) {
        if (viewTreeList == null) {
            viewTreeList = new ArrayList<>(8);
        }
        View parent = view;
        boolean findPage = false;
        do {
            if (ViewAttributeUtil.getCustomId(parent) != null) {
                viewTreeList.add(parent);
                break;
            } else {
                Page<?> page = ViewAttributeUtil.getViewPage(parent);
                if (page != null) {
                    findPage = true;
                    viewTreeList.add(parent);
                    if (!page.isIgnored()) {
                        break;
                    }
                } else {
                    if (!findPage) {
                        viewTreeList.add(parent);
                    }
                }

                if (parent.getParent() instanceof View) {
                    parent = (View) parent.getParent();
                } else {
                    break;
                }
            }
        } while (parent instanceof ViewGroup);
        View rootView = viewTreeList.get(viewTreeList.size() - 1);
        String xpath;
        String originalXpath;

        Page<?> rootPage = ViewAttributeUtil.getViewPage(rootView);
        if (ViewAttributeUtil.getCustomId(rootView) != null) {
            originalXpath = "/" + ViewAttributeUtil.getCustomId(rootView);
            xpath = originalXpath;
        } else if (rootPage != null) {
            originalXpath = WindowHelper.get().getWindowPrefix(rootView);
            xpath = originalXpath;
        } else {
            String prefix = WindowHelper.get().getWindowPrefix(rootView);

            // PopupDecorView 这个class是在Android 6.0的时候才引入的，为了兼容6.0以下的版本，需要手动添加这个class层级
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    && prefix.equals(WindowHelper.POPUP_WINDOW_PREFIX)
                    && !POPUP_DECOR_VIEW_CLASS_NAME.equals(ClassUtil.getSimpleClassName(rootView.getClass()))) {
                xpath = prefix + "/" + POPUP_DECOR_VIEW_CLASS_NAME + "/" + ClassUtil.getSimpleClassName(rootView.getClass()) + "[0]";
            } else {
                xpath = prefix + "/" + ClassUtil.getSimpleClassName(rootView.getClass());
            }
            originalXpath = xpath;
        }

        return ViewNode.ViewNodeBuilder.newViewNode()
                .setView(rootView)
                .setIndex(-1)
                .setViewContent(getViewContent(rootView))
                .setXPath(xpath)
                .setOriginalXPath(originalXpath)
                .setPrefixPage(rootPage == null ? "" : originalXpath)
                .build();
    }

    public static int getChildAdapterPositionInRecyclerView(View childView, ViewGroup parentView) {
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

    private static boolean shouldChangeOn(View view) {
        if (view instanceof EditText) {
            String tag = ViewAttributeUtil.getMonitoringFocusContent(view);
            String lastText = tag == null ? "" : tag;
            String nowText = ((EditText) view).getText().toString();
            if ((TextUtils.isEmpty(nowText) && TextUtils.isEmpty(lastText)) || lastText.equals(nowText)) {
                return false;
            }
            ViewAttributeUtil.setMonitoringFocusContent(view, nowText);
            return true;
        }
        return false;
    }

    @Nullable
    public static ViewNode getChangeViewNode(View view) {
        if (view == null) {
            return null;
        }
        Activity activity = ActivityUtil.findActivity(view.getContext());
        if (activity == null || ViewHelper.isIgnoredView(view) || !shouldChangeOn(view)) {
            return null;
        }

        return getViewNode(view);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static boolean isPasswordInputType(int inputType) {
        final int variation = inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }
}