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
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsSeekBar;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.models.ViewNode;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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

        String xpath;
        String originalXpath;

        if (ViewAttributeUtil.getCustomId(rootView) != null) {
            originalXpath = "/" + ViewAttributeUtil.getCustomId(rootView);
            xpath = originalXpath;
        } else if (ViewAttributeUtil.getViewPage(rootView) != null) {
            originalXpath = PAGE_PREFIX;
            xpath = PAGE_PREFIX;
        } else {
            String prefix = WindowHelper.getWindowPrefix(rootView);
            xpath = prefix + "/" + ClassUtil.getSimpleClassName(rootView.getClass());
            originalXpath = xpath;
        }

        return ViewNode.ViewNodeBuilder.newViewNode()
                .setView(rootView)
                .setIndex(-1)
                .setViewContent(getViewContent(rootView))
                .setXPath(xpath)
                .setOriginalXPath(originalXpath)
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

    private static boolean shouldChangeOn(View view) {
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