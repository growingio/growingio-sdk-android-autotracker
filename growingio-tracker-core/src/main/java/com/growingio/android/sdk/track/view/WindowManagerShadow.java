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

package com.growingio.android.sdk.track.view;

import android.annotation.SuppressLint;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
public class WindowManagerShadow {
    private static final String TAG = "WindowManagerShadow";

    private final Object mRealWindowManager;
    private final Field mViews;
    private final boolean mIsArrayList;

    @SuppressLint("PrivateApi")
    public WindowManagerShadow() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> windowManager = Class.forName("android.view.WindowManagerGlobal");
        Method method = windowManager.getMethod("getInstance");
        method.setAccessible(true);
        mRealWindowManager = method.invoke(null);

        mViews = windowManager.getDeclaredField("mViews");
        mViews.setAccessible(true);
        mIsArrayList = mViews.getType() == ArrayList.class;
    }

    public View[] getAllWindowViews() throws IllegalAccessException {
        View[] views;
        if (mIsArrayList) {
            views = ((ArrayList<View>) mViews.get(mRealWindowManager)).toArray(new View[0]);
        } else {
            views = (View[]) mViews.get(mRealWindowManager);
        }
        return views;
    }
}
