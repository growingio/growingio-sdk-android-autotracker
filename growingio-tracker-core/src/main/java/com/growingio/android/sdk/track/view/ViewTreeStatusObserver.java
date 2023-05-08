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
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.RequiresApi;

import com.growingio.android.sdk.track.R;

public class ViewTreeStatusObserver {

    private final DeprecatedViewStateObserver viewStateObserver;

    private final boolean observeLayout;
    private final boolean observeScroll;
    private final boolean observeFocus;

    private final int defaultTagId;

    public ViewTreeStatusObserver(OnViewStateChangedListener viewStateChangedListener) {
        this(true, true, false, viewStateChangedListener, R.id.growing_tracker_monitoring_view_tree_enabled);
    }

    public ViewTreeStatusObserver(OnViewStateChangedListener viewStateChangedListener, int defaultTagId) {
        this(true, true, false, viewStateChangedListener, defaultTagId);
    }

    public ViewTreeStatusObserver(boolean observeLayout, boolean observeScroll, boolean observeFocus, OnViewStateChangedListener viewStateChangedListener, int defaultTagId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            viewStateObserver = new ViewStateObserver(viewStateChangedListener);
        } else {
            viewStateObserver = new DeprecatedViewStateObserver(viewStateChangedListener);
        }
        this.observeLayout = observeLayout;
        this.observeScroll = observeScroll;
        this.observeFocus = observeFocus;
        this.defaultTagId = defaultTagId;
    }

    public void onActivityResumed(Activity activity) {
        monitorViewTreeChange(activity.getWindow().getDecorView());
    }

    public void onActivityPaused(Activity activity) {
        unRegisterViewTreeChange(activity.getWindow().getDecorView());
    }

    private void unRegisterViewTreeChange(View root) {
        if (isMonitoringViewTree(root)) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(viewStateObserver);
            root.getViewTreeObserver().removeOnGlobalFocusChangeListener(viewStateObserver);
            root.getViewTreeObserver().removeOnScrollChangedListener(viewStateObserver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().removeOnWindowFocusChangeListener((ViewTreeObserver.OnWindowFocusChangeListener) viewStateObserver);
            }
            setMonitoringViewTreeEnabled(root, false);
        }
    }

    private void monitorViewTreeChange(View root) {
        if (!isMonitoringViewTree(root)) {
            if (observeLayout) {
                root.getViewTreeObserver().addOnGlobalLayoutListener(viewStateObserver);
            }
            if (observeScroll) {
                root.getViewTreeObserver().addOnScrollChangedListener(viewStateObserver);
            }
            if (observeFocus) {
                root.getViewTreeObserver().addOnGlobalFocusChangeListener(viewStateObserver);
            }
            if (observeLayout && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().addOnWindowFocusChangeListener((ViewTreeObserver.OnWindowFocusChangeListener) viewStateObserver);
            }

            setMonitoringViewTreeEnabled(root, true);
        }
    }

    private void setMonitoringViewTreeEnabled(View view, boolean monitoring) {
        view.setTag(defaultTagId, monitoring);
    }

    private boolean isMonitoringViewTree(View view) {
        Object monitoring = view.getTag(defaultTagId);
        if (monitoring instanceof Boolean) {
            return (boolean) monitoring;
        }
        return false;
    }

    private static class DeprecatedViewStateObserver implements ViewTreeObserver.OnGlobalLayoutListener,
            ViewTreeObserver.OnScrollChangedListener,
            ViewTreeObserver.OnGlobalFocusChangeListener {

        protected OnViewStateChangedListener viewStateChangedListener;

        DeprecatedViewStateObserver(OnViewStateChangedListener viewStateChangedListener) {
            this.viewStateChangedListener = viewStateChangedListener;
        }

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            viewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.FOCUS_CHANGED, oldFocus, newFocus));
        }

        @Override
        public void onGlobalLayout() {
            viewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
        }

        @Override
        public void onScrollChanged() {
            viewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.SCROLL_CHANGED));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class ViewStateObserver extends DeprecatedViewStateObserver implements ViewTreeObserver.OnWindowFocusChangeListener {

        ViewStateObserver(OnViewStateChangedListener viewStateChangedListener) {
            super(viewStateChangedListener);
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            viewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.WINDOW_FOCUS_CHANGED));
        }
    }
}
