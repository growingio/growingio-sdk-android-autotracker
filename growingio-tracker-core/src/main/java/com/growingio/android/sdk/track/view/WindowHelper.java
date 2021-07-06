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

import android.app.Activity;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

import java.util.ArrayList;
import java.util.List;

public class WindowHelper {
    private static final String TAG = "WindowHelper";


    private final WindowManagerShadow mWindowManager;

    private WindowHelper() {
        WindowManagerShadow managerShadow = null;
        try {
            managerShadow = new WindowManagerShadow("android.view.WindowManagerGlobal");
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        mWindowManager = managerShadow;
    }

    private static class SingleInstance {
        private static final WindowHelper INSTANCE = new WindowHelper();
    }

    public static WindowHelper get() {
        return SingleInstance.INSTANCE;
    }

    public boolean isDecorView(View rootView) {
        return !(rootView.getParent() instanceof View);
    }

    @NonNull
    public List<DecorView> getTopActivityViews() {
        List<DecorView> topViews = new ArrayList<>();
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) return topViews;
        List<DecorView> decorViews = getAllWindowDecorViews();
        boolean findTopActivity = false;
        for (DecorView decorView : decorViews) {
            View view = decorView.getView();
            if (view == activity.getWindow().getDecorView()
                    || view.getContext() == activity) {
                topViews.add(decorView);
                findTopActivity = true;
            } else if (findTopActivity) {
                topViews.add(decorView);
            }
        }
        return topViews;
    }

    public List<DecorView> getAllWindowDecorViews() {
        List<DecorView> decorViews = new ArrayList<>();
        View[] allViews = WindowHelper.get().getWindowViews();
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
        return decorViews;
    }

    @Nullable
    public View getTopActivityDecorView() {
        Activity current = ActivityStateProvider.get().getForegroundActivity();
        if (current != null) {
            try {
                return current.getWindow().getDecorView();
            } catch (Exception e) {
                // Unable to resume activity {xxxActivity}: java.lang.RuntimeException: Window couldn't find content container view
                Logger.e(TAG, e);
            }
        }
        return null;
    }

    private View[] getWindowViews() {
        if (mWindowManager != null) {
            try {
                return mWindowManager.getAllWindowViews();
            } catch (IllegalAccessException e) {
                Logger.e(TAG, e);
            }
        }

        // 如果无法获取WindowManager就只遍历当前Activity的内容
        View decorView = getTopActivityDecorView();
        if (decorView != null) {
            return new View[]{decorView};
        }

        return new View[0];
    }
}
