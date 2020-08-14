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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.view.menu.ListMenuItemView;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class WindowHelper {

    public static final String PAGE_PREFIX = "/Page";
    private static final String MAIN_WINDOW_PREFIX = "/MainWindow";
    private static final String DIALOG_WINDOW_PREFIX = "/DialogWindow";
    private static final String POPUP_WINDOW_PREFIX = "/PopupWindow";
    private static final String CUSTOM_WINDOW_PREFIX = "/CustomWindow";
    static Object sWindowManger;
    static Field sViewsField;
    static Field sWindowField;
    static Class sPhoneWindowClazz;
    static Class sPopupWindowClazz;
    static boolean sArrayListWindowViews = false;
    static boolean sViewArrayWindowViews = false;
    @VisibleForTesting
    static WeakHashMap<View, Long> sShowingToast = new WeakHashMap<>();
    private static Class<?> sListMenuItemViewClazz;
    private static Method sItemViewGetDataMethod;
    private static boolean sIsInitialized = false;

    private WindowHelper() {
    }

    public static void init() {
        if (sIsInitialized) {
            return;
        }
        String windowManagerClassName;
        if (Build.VERSION.SDK_INT >= 17) {
            windowManagerClassName = "android.view.WindowManagerGlobal";
        } else {
            windowManagerClassName = "android.view.WindowManagerImpl";
        }

        Class<?> windowManager = null;
        try {
            windowManager = Class.forName(windowManagerClassName);

            String windowManagerString;
            if (Build.VERSION.SDK_INT >= 17) {
                windowManagerString = "sDefaultWindowManager";
            } else if (Build.VERSION.SDK_INT >= 13) {
                windowManagerString = "sWindowManager";
            } else {
                windowManagerString = "mWindowManager";
            }

            sViewsField = windowManager.getDeclaredField("mViews");

            Field instanceField = windowManager.getDeclaredField(windowManagerString);
            sViewsField.setAccessible(true);
            if (sViewsField.getType() == ArrayList.class) {
                sArrayListWindowViews = true;
            } else if (sViewsField.getType() == View[].class) {
                sViewArrayWindowViews = true;
            }
            instanceField.setAccessible(true);
            sWindowManger = instanceField.get(null);

        } catch (NoSuchFieldException e) {
            LogUtil.d(e);
        } catch (IllegalAccessException e) {
            LogUtil.d(e);
        } catch (ClassNotFoundException e) {
            LogUtil.d(e);
        }

        try {
            sListMenuItemViewClazz = Class.forName("com.android.internal.view.menu.ListMenuItemView");
            Class itemViewInterface = Class.forName("com.android.internal.view.menu.MenuView$ItemView");
            sItemViewGetDataMethod = itemViewInterface.getDeclaredMethod("getItemData");
        } catch (ClassNotFoundException e) {
            LogUtil.d(e);
        } catch (NoSuchMethodException e) {
            LogUtil.d(e);
        }

        try {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    sPhoneWindowClazz = Class.forName("com.android.internal.policy.PhoneWindow$DecorView");
                } catch (ClassNotFoundException exception) {
                    // for Android N
                    sPhoneWindowClazz = Class.forName("com.android.internal.policy.DecorView");
                }
            } else {
                sPhoneWindowClazz = Class.forName("com.android.internal.policy.impl.PhoneWindow$DecorView");
            }
        } catch (ClassNotFoundException e) {
            LogUtil.d(e);
        }
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                sPopupWindowClazz = Class.forName("android.widget.PopupWindow$PopupDecorView");
            } else {
                sPopupWindowClazz = Class.forName("android.widget.PopupWindow$PopupViewContainer");
            }
        } catch (ClassNotFoundException e) {
            LogUtil.d(e);
        }
        sIsInitialized = true;
    }

    public static boolean isDecorView(View rootView) {
        if (!sIsInitialized) {
            init();
        }
        Class rootClass = rootView.getClass();
        return rootClass == sPhoneWindowClazz || rootClass == sPopupWindowClazz;
    }

    @SuppressLint("RestrictedApi")
    public static Object getMenuItemData(View view) throws InvocationTargetException, IllegalAccessException {
        if (view.getClass() == sListMenuItemViewClazz) {
            return sItemViewGetDataMethod.invoke(view);
        } else if (ClassExistHelper.instanceOfAndroidXListMenuItemView(view)) {
            return ((ListMenuItemView) view).getItemData();
        } else if (ClassExistHelper.instanceOfSupportListMenuItemView(view)) {
            return ((android.support.v7.view.menu.ListMenuItemView) view).getItemData();
        }
        return null;
    }

    public static String getMainWindowPrefix() {
        return MAIN_WINDOW_PREFIX;
    }

    public static String getWindowPrefix(View root) {
        Page<?> page = ViewAttributeUtil.getViewPage(root);
        if (page != null) {
            return PAGE_PREFIX;
        }


        if (root.hashCode() == ActivityStateProvider.get().getCurrentRootWindowsHashCode()) {
            return getMainWindowPrefix();
        }

        return getSubWindowPrefix(root);
    }

    public static String getSubWindowPrefix(View root) {
        ViewGroup.LayoutParams params = root.getLayoutParams();
        if (params != null && params instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) params;
            int type = windowParams.type;
            if (type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) {
                return MAIN_WINDOW_PREFIX;
            } else if (type < WindowManager.LayoutParams.LAST_APPLICATION_WINDOW && root.getClass() == sPhoneWindowClazz) {
                return DIALOG_WINDOW_PREFIX;
            } else if (type < WindowManager.LayoutParams.LAST_SUB_WINDOW && root.getClass() == sPopupWindowClazz) {
                return POPUP_WINDOW_PREFIX;
            } else if (type < WindowManager.LayoutParams.LAST_SYSTEM_WINDOW) {
                return CUSTOM_WINDOW_PREFIX;
            }
        }
        // if get WindowManager.LayoutParams failed, use Class type as fallback.
        Class rootClazz = root.getClass();
        if (rootClazz == sPhoneWindowClazz) {
            return DIALOG_WINDOW_PREFIX;
        } else if (rootClazz == sPopupWindowClazz) {
            return POPUP_WINDOW_PREFIX;
        } else {
            return CUSTOM_WINDOW_PREFIX;
        }
    }

    @NonNull
    public static DecorView[] getTopActivityViews() {
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) {
            return new DecorView[0];
        }

        List<DecorView> topViews = new ArrayList<>();
        for (DecorView decorView : getAllWindowDecorViews()) {
            View view = decorView.getView();
            if (view == activity.getWindow().getDecorView()
                    || view.getContext() == activity
                    || ActivityUtil.findActivity(view.getContext()) == activity) {
                topViews.add(decorView);
            }
        }
        return topViews.toArray(new DecorView[0]);
    }

    public static DecorView[] getTopWindowViews() {
        List<DecorView> topViews = new ArrayList<>();
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        DecorView[] decorViews = getAllWindowDecorViews();
        for (DecorView decorView : decorViews) {
            View view = decorView.getView();
            if (view == activity.getWindow().getDecorView()
                    || view.getContext() == activity) {
                topViews.add(decorView);
            } else if ((view.getContext() instanceof ContextWrapper && ((ContextWrapper) view.getContext()).getBaseContext() == activity)) {
                topViews.clear();
                topViews.add(decorView);
            }
        }
        return topViews.toArray(new DecorView[0]);
    }

    public static DecorView[] getAllWindowDecorViews() {
        List<DecorView> decorViews = new ArrayList<>();
        View[] allViews = WindowHelper.getWindowViews();
        for (View view : allViews) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];

            Rect area = new Rect(x, y, x + view.getWidth(), y + view.getHeight());
            if (view.getLayoutParams() instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) view.getLayoutParams();
                decorViews.add(new DecorView(view, area, windowParams));
            }
        }
        return decorViews.toArray(new DecorView[0]);
    }

    public static View[] getWindowViews() {
        init();
        View[] result = new View[0];
        if (sWindowManger == null) {
            // 如果无法获取WindowManager就只遍历当前Activity的内容
            Activity current = ActivityStateProvider.get().getForegroundActivity();
            if (current != null) {
                return new View[]{current.getWindow().getDecorView()};
            }
            return result;
        }
        try {
            View[] views = null;
            if (sArrayListWindowViews) {
                views = ((ArrayList<View>) sViewsField.get(sWindowManger)).toArray(result);
            } else if (sViewArrayWindowViews) {
                views = (View[]) sViewsField.get(sWindowManger);
            }
            if (views != null) {
                // views可能会得到空值,可以考虑不从WindowManager反射获取所有Window
                result = views;
            }
        } catch (Exception e) {
            LogUtil.d(e);
        }
        return filterNullAndDismissToastView(result);
    }

    public static void onToastShow(@NonNull Toast toast) {
        try {
            View nextView = toast.getView();
            int duration = toast.getDuration();
            if (nextView == null) {
                return;
            }
            // 不需要知道精确时间, 大致给个LONG_DURATION + 10000 = 8000
            duration = Math.max(8000, duration);
            sShowingToast.put(nextView, duration + System.currentTimeMillis());
        } catch (Exception e) {
            LogUtil.d("GIO.Window", "onToastShow, failed: ", e);
        }
    }

    public static View[] filterNullAndDismissToastView(View[] array) {
        List<View> list = new ArrayList<>(array.length);
        long currentTime = System.currentTimeMillis();
        for (View view : array) {
            if (view == null) {
                continue;
            }
            if (!sShowingToast.isEmpty()) {
                Long deadline = sShowingToast.get(view);
                if (deadline != null && currentTime > deadline) {
                    continue;
                }
            }
            list.add(view);
        }
        View[] result = new View[list.size()];
        list.toArray(result);
        return result;
    }
}
