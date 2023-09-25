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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;

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

    Bitmap tryRenderDialog(Activity activity, Bitmap original) {
        List<DecorView> views = mWindowManager.getFloatingWindow(activity);
        if (views.isEmpty()) return original;
        try {
            Canvas canvas = new Canvas(original);
            for (DecorView view : views) {
                if (!view.isDialog()) {
                    drawPanel(canvas, view);
                }
            }

            for (DecorView dialog : views) {
                if (dialog.isDialog()) {
                    int dimColorAlpha = (int) (dialog.getLayoutParams().dimAmount * 255);
                    canvas.drawColor(Color.argb(dimColorAlpha, 0, 0, 0));
                    drawPanel(canvas, dialog);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return original;
    }

    private void drawPanel(Canvas canvas, DecorView info) {
        View panelView = info.getView();
        if (panelView.getWidth() == 0 || panelView.getHeight() == 0) {
            return;
        }
        canvas.save();
        canvas.translate(info.getRect().left * 1.0f, info.getRect().top * 1.0f);
        panelView.draw(canvas);
        canvas.restore();
    }

    public boolean isDecorView(View rootView) {
        return !(rootView.getParent() instanceof View);
    }

    public List<DecorView> getTopActivityViews() {
        List<DecorView> topViews = new ArrayList<>();
        Activity activity = TrackMainThread.trackMain().getForegroundActivity();
        if (activity == null) return null;
        List<DecorView> decorViews = getAllWindowDecorViews();
        View activityView = activity.getWindow().getDecorView();
        for (DecorView decorView : decorViews) {
            View view = decorView.getView();
            if (view == activityView || view.getContext() == activity) {
                topViews.add(decorView);
            } else if (decorView.getView().getWidth() < activityView.getWidth() && decorView.getView().getHeight() < activityView.getHeight()) {
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

    public View getTopActivityDecorView() {
        Activity current = TrackMainThread.trackMain().getForegroundActivity();
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
            } catch (Exception e) {
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
