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
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

public class ViewTreeStatusProvider extends ListenerContainer<OnViewStateChangedListener, ViewStateChangedEvent> implements IActivityLifecycle {
    private static final String TAG = "ViewTreeStatusProvider";

    private final DeprecatedViewStateObserver mViewStateObserver;

    private static class SingleInstance {
        private static final ViewTreeStatusProvider INSTANCE = new ViewTreeStatusProvider();
    }

    public static ViewTreeStatusProvider get() {
        return SingleInstance.INSTANCE;
    }

    public ViewTreeStatusProvider() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mViewStateObserver = new ViewStateObserver();
        } else {
            mViewStateObserver = new DeprecatedViewStateObserver();
        }
    }

    @Override
    protected void singleAction(OnViewStateChangedListener listener, ViewStateChangedEvent event) {
        listener.onViewStateChanged(event);
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            monitorViewTreeChange(activity.getWindow().getDecorView());
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            unRegisterViewTreeChange(activity.getWindow().getDecorView());
        }
    }

    private void unRegisterViewTreeChange(View root) {
        if (isMonitoringViewTree(root)) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(mViewStateObserver);
            root.getViewTreeObserver().removeOnGlobalFocusChangeListener(mViewStateObserver);
            root.getViewTreeObserver().removeOnScrollChangedListener(mViewStateObserver);
            root.getViewTreeObserver().removeOnDrawListener(mViewStateObserver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().removeOnWindowFocusChangeListener((ViewTreeObserver.OnWindowFocusChangeListener) mViewStateObserver);
            }
            setMonitoringViewTreeEnabled(root, false);
        }
    }

    private void monitorViewTreeChange(View root) {
        if (!isMonitoringViewTree(root)) {
            root.getViewTreeObserver().addOnGlobalLayoutListener(mViewStateObserver);
            root.getViewTreeObserver().addOnScrollChangedListener(mViewStateObserver);
            root.getViewTreeObserver().addOnGlobalFocusChangeListener(mViewStateObserver);
            root.getViewTreeObserver().addOnDrawListener(mViewStateObserver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().addOnWindowFocusChangeListener((ViewTreeObserver.OnWindowFocusChangeListener) mViewStateObserver);
            }

            setMonitoringViewTreeEnabled(root, true);
        }
    }

    private static class DeprecatedViewStateObserver implements ViewTreeObserver.OnGlobalLayoutListener,
            ViewTreeObserver.OnScrollChangedListener,
            ViewTreeObserver.OnGlobalFocusChangeListener,
            ViewTreeObserver.OnDrawListener {

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            ViewTreeStatusProvider.get().dispatchActions(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.FOCUS_CHANGED, oldFocus, newFocus));
        }

        @Override
        public void onGlobalLayout() {
            ViewTreeStatusProvider.get().dispatchActions(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
        }

        @Override
        public void onScrollChanged() {
            ViewTreeStatusProvider.get().dispatchActions(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.SCROLL_CHANGED));
        }

        @Override
        public void onDraw() {
            ViewTreeStatusProvider.get().dispatchActions(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.DRAW));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class ViewStateObserver extends DeprecatedViewStateObserver implements ViewTreeObserver.OnWindowFocusChangeListener {
        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            ViewTreeStatusProvider.get().dispatchActions(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.WINDOW_FOCUS_CHANGED));
        }
    }

    public static void setMonitoringViewTreeEnabled(View view, boolean monitoring) {
        if (monitoring) {
            view.setTag(R.id.growing_tracker_monitoring_view_tree_enabled, new Object());
        } else {
            view.setTag(R.id.growing_tracker_monitoring_view_tree_enabled, null);
        }
    }

    public static boolean isMonitoringViewTree(View view) {
        Object monitoring = view.getTag(R.id.growing_tracker_monitoring_view_tree_enabled);
        return monitoring != null;
    }
}