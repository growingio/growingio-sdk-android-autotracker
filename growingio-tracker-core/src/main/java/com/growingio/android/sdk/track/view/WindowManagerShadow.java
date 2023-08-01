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
package com.growingio.android.sdk.track.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.growingio.android.sdk.track.log.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Android 4.2
 * private static WindowManagerGlobal sDefaultWindowManager;
 * private static IWindowManager sWindowManagerService;
 * private View[] mViews;
 * .
 * Android 4.4
 * private static WindowManagerGlobal sDefaultWindowManager;
 * private static IWindowManager sWindowManagerService;
 * private final ArrayList<View> mViews = new ArrayList<View>();
 * .
 * Android 11 bate1版本 RPB1.200504.018 删除了 sDefaultWindowManager, RC又恢复了，不敢再用
 */
class WindowManagerShadow {
    private static final String TAG = "WindowManagerShadow";

    private Object realWindowManager;
    private final Map<CacheFieldKey, Field> fieldsMap = new HashMap<>();
    private final int[] outLocation = new int[2];
    private final String wmClassName;

    WindowManagerShadow(String wmClassName) {
        this.wmClassName = wmClassName;
    }

    public View[] getAllWindowViews() throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Object wm = realWindowManager;
        if (wm == null) {
            Class<?> windowManager = Class.forName(wmClassName);
            Method method = windowManager.getMethod("getInstance");
            method.setAccessible(true);
            wm = method.invoke(null);
            realWindowManager = wm;
        }
        View[] views;
        Field mViews = getNotNullField("mViews", realWindowManager);
        mViews.setAccessible(true);
        boolean mIsArrayList = mViews.getType() == ArrayList.class;
        if (mIsArrayList) {
            views = ((ArrayList<View>) mViews.get(realWindowManager)).toArray(new View[0]);
        } else {
            views = (View[]) mViews.get(realWindowManager);
        }
        return views;
    }

    public List<DecorView> getFloatingWindow(Activity activity) {
        try {
            return getFloatingWindowInternal(activity);
        } catch (Exception e) {
            Logger.e(TAG, e);
            return new ArrayList<>();
        }
    }

    private List<DecorView> getFloatingWindowInternal(Activity activity) throws NoSuchFieldException, IllegalAccessException {
        Object globalWindowManager = getGlobalWindowManager(activity);
        Object viewRootsObject = getNotNullFieldValue("mRoots", globalWindowManager);
        Object paramsObject = getNotNullFieldValue("mParams", globalWindowManager);

        Object[] viewRootImpls = asViewRootsArray(viewRootsObject);
        WindowManager.LayoutParams[] params = asWindowLayoutParamsArray(paramsObject);
        if (viewRootImpls == null || params == null) {
            IllegalStateException e = new IllegalStateException("failed to get view roots or params");
            Logger.e(TAG, e);
            return new ArrayList<>();
        }
        return collectData(activity, viewRootImpls, params);
    }

    private List<DecorView> collectData(Activity activity, Object[] roots, WindowManager.LayoutParams[] params) throws NoSuchFieldException, IllegalAccessException {
        List<DecorView> rootViews = new ArrayList<>();
        for (int i = 0; i < roots.length; i++) {
            // isActivity
            if (params[i].type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) {
                continue;
            }
            Object root = roots[i];
            Object objectView = getNotNullFieldValue("mView", root);
            if (objectView == null || objectView instanceof View) {
                Logger.e(TAG, "null View or Window stored in Global window manager, skipping");
            }
            View view = (View) objectView;
            Context activityContext = contextIsActivity(view.getContext());
            if (activityContext != activity || !view.isShown()) {
                continue;
            }
            rootViews.add(new DecorView(view, getViewRect(view), params[i]));
        }
        return rootViews;
    }

    private Context contextIsActivity(Context context) {
        Context result = context;
        while (result != null) {
            if (result instanceof Activity) {
                return result;
            } else if (result instanceof ContextWrapper) {
                result = ((ContextWrapper) result).getBaseContext();
            } else {
                return null;
            }
        }
        return null;
    }

    private Rect getViewRect(View view) {
        view.getLocationOnScreen(outLocation);
        int left = outLocation[0];
        int top = outLocation[1];
        return new Rect(left, top, left + view.getWidth(), top + view.getHeight());
    }

    @SuppressLint("ObsoleteSdkInt")
    private Object getGlobalWindowManager(Activity activity) throws NoSuchFieldException, IllegalAccessException {
        Object wm = realWindowManager;
        if (wm == null) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                wm = getNotNullFieldValue("mWindowManager", activity.getWindowManager());
            } else {
                wm = getNotNullFieldValue("mGlobal", activity.getWindowManager());
            }
            realWindowManager = wm;
        }
        return wm;
    }

    private Object getNotNullFieldValue(String fieldName, Object target) throws NoSuchFieldException, IllegalAccessException {
        Field field = getNotNullField(fieldName, target);
        return field.get(target);
    }

    private Field getNotNullField(String fieldName, Object target) throws NoSuchFieldException {
        CacheFieldKey key = new CacheFieldKey(target.getClass(), fieldName);
        if (fieldsMap.containsKey(key)) {
            return fieldsMap.get(key);
        } else {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        field.setAccessible(true);
                        fieldsMap.put(key, field);
                        return field;
                    }
                }
                clazz = clazz.getSuperclass();
            }
            throw new NoSuchFieldException("Field " + fieldName + " not found for class " + clazz);
        }
    }

    private WindowManager.LayoutParams[] asWindowLayoutParamsArray(Object params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            List<WindowManager.LayoutParams> paramsList = (List<WindowManager.LayoutParams>) params;
            return paramsList.toArray(new WindowManager.LayoutParams[paramsList.size()]);
        } else {
            return (WindowManager.LayoutParams[]) params;
        }
    }

    private Object[] asViewRootsArray(Object viewRootImpls) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            List<Object> list = (List<Object>) viewRootImpls;
            return list.toArray();
        } else {
            return (Object[]) viewRootImpls;
        }
    }

    private static class CacheFieldKey {
        public Class<?> clazz;
        public String fieldName;

        private CacheFieldKey(Class<?> clazz, String fieldName) {
            this.clazz = clazz;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheFieldKey that = (CacheFieldKey) o;
            return Objects.equals(clazz, that.clazz) && fieldName.equals(that.fieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, fieldName);
        }
    }
}
