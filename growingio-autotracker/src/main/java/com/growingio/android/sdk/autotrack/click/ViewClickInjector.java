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

package com.growingio.android.sdk.autotrack.click;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toolbar;


import com.growingio.android.sdk.autotrack.models.ViewNode;
import com.growingio.android.sdk.autotrack.util.ViewHelper;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.SysTrace;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.sdk.inject.annotation.BeforeSuper;

public class ViewClickInjector {
    private static final String TAG = "ViewClickInjector";
    private static final ThreadLocal<Boolean> NOT_HANDLE_CLICK_RESULT = new ThreadLocal<>();
    private static final PersistClickEventRunnable PERSIST_CLICK_EVENT_RUNNABLE = new PersistClickEventRunnable();

    private ViewClickInjector() {
    }

    @BeforeSuper(clazz = View.OnClickListener.class, method = "onClick", parameterTypes = {View.class})
    public static void beforeViewOnClick(View.OnClickListener listener, View view) {
        clickOn(view);
    }

    @BeforeSuper(clazz = DialogInterface.OnClickListener.class, method = "onClick", parameterTypes = {DialogInterface.class, int.class})
    public static void beforeViewOnClick(DialogInterface.OnClickListener listener, DialogInterface dialogInterface, int which) {
        try {
            if (dialogInterface instanceof AlertDialog) {
                clickOn(((AlertDialog) dialogInterface).getButton(which));
            }
        } catch (Exception e) {
            LogUtil.d(e);
        }
    }

    @BeforeSuper(clazz = AdapterView.OnItemClickListener.class, method = "onItemClick", parameterTypes = {AdapterView.class, View.class, int.class, long.class})
    public static void beforeViewOnClick(AdapterView.OnItemClickListener listener, AdapterView adapterView, View view, int position, long id) {
        if (adapterView instanceof Spinner) {
            clickOn(adapterView);
        } else {
            clickOn(view);
        }
    }

    @BeforeSuper(clazz = AdapterView.OnItemSelectedListener.class, method = "onItemSelected", parameterTypes = {AdapterView.class, View.class, int.class, long.class})
    public static void beforeViewOnClick(AdapterView.OnItemSelectedListener listener, AdapterView adapterView, View view, int position, long id) {
        if (adapterView instanceof Spinner) {
            // 目前只需要将Spinner的onItemSelected回调触发点击事件,因为Spinner的元素点击只会触发onItemSelected回调
            beforeViewOnClick(listener, adapterView, view, position, id);
        }
    }

    @BeforeSuper(clazz = MenuItem.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class})
    public static void beforeViewOnClick(MenuItem.OnMenuItemClickListener listener, MenuItem item) {
        NOT_HANDLE_CLICK_RESULT.set(true);
        onMenuItemClick(item);
        NOT_HANDLE_CLICK_RESULT.set(false);
    }

    @BeforeSuper(clazz = Toolbar.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class})
    public static void beforeViewOnClick(Toolbar.OnMenuItemClickListener listener, MenuItem item) {
        NOT_HANDLE_CLICK_RESULT.set(true);
        onMenuItemClick(item);
        NOT_HANDLE_CLICK_RESULT.set(false);
    }

    @BeforeSuper(clazz = ActionMenuView.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class})
    public static void beforeViewOnClick(ActionMenuView.OnMenuItemClickListener listener, MenuItem item) {
        NOT_HANDLE_CLICK_RESULT.set(true);
        onMenuItemClick(item);
        NOT_HANDLE_CLICK_RESULT.set(false);
    }

    @BeforeSuper(clazz = PopupMenu.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class})
    public static void beforeViewOnClick(PopupMenu.OnMenuItemClickListener listener, MenuItem item) {
        NOT_HANDLE_CLICK_RESULT.set(true);
        onMenuItemClick(item);
        NOT_HANDLE_CLICK_RESULT.set(false);
    }

    @BeforeSuper(clazz = Activity.class, method = "onOptionsItemSelected", parameterTypes = {MenuItem.class})
    public static void beforeViewOnClick(Activity activity, MenuItem item) {
        if (!GConfig.getInstance().isInitSucceeded() || PERSIST_CLICK_EVENT_RUNNABLE.havePendingEvent()) {
            return;
        }

        ViewNode viewNode = null;

        if (!ClassExistHelper.instanceOfAndroidXFragmentActivity(activity)
                && !ClassExistHelper.instanceOfSupportFragmentActivity(activity)) {
            viewNode = ViewHelper.getClickViewNode(item);
        }

        PERSIST_CLICK_EVENT_RUNNABLE.resetData(viewNode);
    }

    @BeforeSuper(clazz = ExpandableListView.OnGroupClickListener.class, method = "onGroupClick", parameterTypes = {ExpandableListView.class, View.class, int.class, long.class})
    public static void beforeViewOnClick(ExpandableListView.OnGroupClickListener listener, AdapterView adapterView, View view, int groupPosition, long id) {
        NOT_HANDLE_CLICK_RESULT.set(true);
        try {
            if (!GConfig.getInstance().isInitSucceeded() || PERSIST_CLICK_EVENT_RUNNABLE.havePendingEvent()) {
                return;
            }

            ViewNode viewNode = ViewHelper.getClickViewNode(view);
            PERSIST_CLICK_EVENT_RUNNABLE.resetData(viewNode);
            if (!threadLocalResult(NOT_HANDLE_CLICK_RESULT)) {
                handleClickResult(true);
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, e, e.getMessage());
        }
        NOT_HANDLE_CLICK_RESULT.set(false);
    }

    @BeforeSuper(clazz = ExpandableListView.OnChildClickListener.class, method = "onChildClick", parameterTypes = {ExpandableListView.class, View.class, int.class, int.class, long.class})
    public static void beforeViewOnClick(ExpandableListView.OnChildClickListener listener, AdapterView adapterView, View view, int groupPosition, int childPosition, long id) {
        NOT_HANDLE_CLICK_RESULT.set(true);
        try {
            if (!GConfig.getInstance().isInitSucceeded() || PERSIST_CLICK_EVENT_RUNNABLE.havePendingEvent()) {
                return;
            }
            ViewNode viewNode = ViewHelper.getClickViewNode(view);
            PERSIST_CLICK_EVENT_RUNNABLE.resetData(viewNode);
            if (!threadLocalResult(NOT_HANDLE_CLICK_RESULT)) {
                handleClickResult(true);
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, e, e.getMessage());
        }
        NOT_HANDLE_CLICK_RESULT.set(false);
    }

    @BeforeSuper(clazz = ListActivity.class, method = "onListItemClick", parameterTypes = {ListView.class, View.class, int.class, long.class})
    public static void beforeViewOnClick(ListActivity activity, ListView listView, View view, int position, long id) {
        clickOn(view);
    }

    @BeforeSuper(clazz = CompoundButton.OnCheckedChangeListener.class, method = "onCheckedChanged", parameterTypes = {CompoundButton.class, boolean.class})
    public static void beforeViewOnClick(CompoundButton.OnCheckedChangeListener listener, CompoundButton button, boolean checked) {
        clickOn(button);
    }

    @BeforeSuper(clazz = RadioGroup.OnCheckedChangeListener.class, method = "onCheckedChanged", parameterTypes = {RadioGroup.class, int.class})
    public static void beforeViewOnClick(RadioGroup.OnCheckedChangeListener listener, RadioGroup radioGroup, int i) {
        try {
            View childView = (View) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            clickOn(childView);
        } catch (Throwable e) {
            LogUtil.e(TAG, e, e.getMessage());
        }
    }

    @BeforeSuper(clazz = RatingBar.OnRatingBarChangeListener.class, method = "onRatingChanged", parameterTypes = {RatingBar.class, float.class, boolean.class})
    public static void beforeViewOnClick(RatingBar.OnRatingBarChangeListener listener, RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            clickOn(ratingBar);
        }
    }

    @BeforeSuper(clazz = SeekBar.OnSeekBarChangeListener.class, method = "onStopTrackingTouch", parameterTypes = {SeekBar.class})
    public static void beforeViewOnClick(SeekBar.OnSeekBarChangeListener listener, SeekBar seekBar) {
        clickOn(seekBar);
    }

    public static void onMenuItemClick(MenuItem menuItem) {
        try {
            if (!GConfig.getInstance().isInitSucceeded() || PERSIST_CLICK_EVENT_RUNNABLE.havePendingEvent()) {
                return;
            }
            ViewNode viewNode = ViewHelper.getClickViewNode(menuItem);
            PERSIST_CLICK_EVENT_RUNNABLE.resetData(viewNode);
            if (!threadLocalResult(NOT_HANDLE_CLICK_RESULT)) {
                handleClickResult(true);
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, e, e.getMessage());
        }
    }

    private static boolean threadLocalResult(ThreadLocal<Boolean> threadLocal) {
        return threadLocal.get() != null && threadLocal.get();
    }

    public static void handleClickResult(Object returnValueObject) {
        boolean result = handleBooleanResult(returnValueObject);

        if (result && PERSIST_CLICK_EVENT_RUNNABLE.havePendingEvent()) {
            ThreadUtils.cancelTaskOnUiThread(PERSIST_CLICK_EVENT_RUNNABLE);
            ThreadUtils.postOnUiThread(PERSIST_CLICK_EVENT_RUNNABLE);
        } else {
            PERSIST_CLICK_EVENT_RUNNABLE.resetData(null);
        }
    }

    private static boolean handleBooleanResult(Object returnValueObject) {
        boolean result = false;

        if (returnValueObject instanceof Boolean) {
            result = (Boolean) returnValueObject;
        }

        return result;
    }

    public static void clickOn(View view) {
        if (GConfig.getInstance().isInitSucceeded()) {
            try {
                SysTrace.beginSection("gio.Click");
                ViewNode viewNode = ViewHelper.getClickViewNode(view);
                if (viewNode == null) {
                    return;
                }
                ViewHelper.persistClickEvent(ViewHelper.getClickActionEvent(viewNode));

            } catch (Throwable e) {
                LogUtil.e(TAG, e);
            } finally {
                SysTrace.endSection();
            }
        }
    }

    private static class PersistClickEventRunnable implements Runnable {
        private ViewNode mViewNode;
        private BaseEvent.BaseEventBuilder<?> mActionEvent;

        public void resetData(ViewNode viewNode) {
            this.mViewNode = viewNode;

            if (viewNode != null) {
                mActionEvent = ViewHelper.getClickActionEvent(viewNode);
            }
        }

        public boolean havePendingEvent() {
            return mViewNode != null;
        }

        @Override
        public void run() {
            try {
                ViewHelper.persistClickEvent(mActionEvent);
            } catch (Throwable e) {
                LogUtil.d(e);
            }

            mViewNode = null;
        }
    }
}
