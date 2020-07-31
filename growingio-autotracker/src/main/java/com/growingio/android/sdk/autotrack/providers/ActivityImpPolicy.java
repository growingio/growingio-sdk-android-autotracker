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

package com.growingio.android.sdk.autotrack.providers;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.growingio.android.sdk.autotrack.util.ViewAttributeUtil;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.status.ViewTreeStatusObservable;

public class ActivityImpPolicy implements IActivityLifecycle {

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            monitorViewTreeChange(activity.getWindow().getDecorView());
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            unRegisterViewTreeChange(activity.getWindow().getDecorView());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void unRegisterViewTreeChange(View root) {
        if (ViewAttributeUtil.isMonitoringViewTree(root)) {
            ViewTreeStatusObservable observable = ViewTreeStatusObservable.getInstance();
            root.getViewTreeObserver().removeOnGlobalLayoutListener(observable);
            root.getViewTreeObserver().removeOnGlobalFocusChangeListener(observable);
            root.getViewTreeObserver().removeOnScrollChangedListener(observable);
            root.getViewTreeObserver().removeOnDrawListener(observable);
            ViewAttributeUtil.setMonitoringViewTreeEnabled(root, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().removeOnWindowFocusChangeListener(ViewTreeStatusObservable.FocusListener.getInstance());
            }
        }
    }

    private void monitorViewTreeChange(View root) {
        if (!ViewAttributeUtil.isMonitoringViewTree(root)) {
            root.getViewTreeObserver().addOnGlobalLayoutListener(ViewTreeStatusObservable.getInstance());
            root.getViewTreeObserver().addOnScrollChangedListener(ViewTreeStatusObservable.getInstance());
            root.getViewTreeObserver().addOnGlobalFocusChangeListener(ViewTreeStatusObservable.getInstance());
            root.getViewTreeObserver().addOnDrawListener(ViewTreeStatusObservable.getInstance());
            ViewAttributeUtil.setMonitoringViewTreeEnabled(root, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().addOnWindowFocusChangeListener(ViewTreeStatusObservable.FocusListener.getInstance());
            }
        }
    }
}
