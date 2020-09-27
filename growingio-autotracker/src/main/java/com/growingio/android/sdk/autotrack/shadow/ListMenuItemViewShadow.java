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

package com.growingio.android.sdk.autotrack.shadow;

import android.annotation.SuppressLint;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

import java.lang.reflect.Method;

public class ListMenuItemViewShadow {
    private static final String TAG = "ListMenuItemViewShadow";

    private static Class<?> sListMenuItemViewClazz;
    private static Method sItemViewGetDataMethod;
    private final View mView;

    static {
        try {
            sListMenuItemViewClazz = Class.forName("com.android.internal.view.menu.ListMenuItemView");
            Class<?> itemViewInterface = Class.forName("com.android.internal.view.menu.MenuView$ItemView");
            sItemViewGetDataMethod = itemViewInterface.getDeclaredMethod("getItemData");
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    public ListMenuItemViewShadow(View view) {
        mView = view;
    }

    public static boolean isListMenuItemView(View view) {
        return view.getClass() == sListMenuItemViewClazz
                || ClassExistHelper.instanceOfSupportListMenuItemView(view)
                || ClassExistHelper.instanceOfAndroidXListMenuItemView(view);
    }

    @SuppressLint("RestrictedApi")
    @Nullable
    public MenuItem getMenuItem() {
        View view = mView;
        if (view == null) {
            return null;
        }
        if (view.getClass() == sListMenuItemViewClazz) {
            try {
                return (MenuItem) sItemViewGetDataMethod.invoke(view);
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        } else if (ClassExistHelper.instanceOfAndroidXListMenuItemView(view)) {
            return ((androidx.appcompat.view.menu.ListMenuItemView) view).getItemData();
        } else if (ClassExistHelper.instanceOfSupportListMenuItemView(view)) {
            return ((android.support.v7.view.menu.ListMenuItemView) view).getItemData();
        }
        return null;
    }
}
