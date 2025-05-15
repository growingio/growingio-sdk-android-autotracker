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
package com.growingio.android.sdk.autotrack.inject;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.R;
import com.growingio.android.sdk.autotrack.view.ViewNodeProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.view.OnViewStateChangedListener;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.view.ViewTreeStatusObserver;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

public class ViewChangeProvider implements IActivityLifecycle, OnViewStateChangedListener, TrackerLifecycleProvider {
    private static final String TAG = "ViewChangeProvider";

    ViewChangeProvider() {
    }

    private ViewTreeStatusObserver viewTreeStatusObserver;
    private ActivityStateProvider activityStateProvider;
    private ViewNodeProvider viewNodeProvider;

    private AutotrackConfig autotrackConfig;


    @Override
    public void setup(TrackerContext context) {
        autotrackConfig = context.getConfigurationProvider().getConfiguration(AutotrackConfig.class);
        activityStateProvider = context.getActivityStateProvider();
        viewNodeProvider = context.getProvider(ViewNodeProvider.class);

        if (autotrackConfig == null || !autotrackConfig.isAutotrack()) return;
        activityStateProvider.registerActivityLifecycleListener(this);
        viewTreeStatusObserver = new ViewTreeStatusObserver(false, false, true, false, this,
                R.id.growing_tracker_monitoring_focus_change);
    }

    @Override
    public void shutdown() {
        activityStateProvider.unregisterActivityLifecycleListener(this);
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (activity == null || viewTreeStatusObserver == null) {
            return;
        }
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            viewTreeStatusObserver.onActivityResumed(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            viewTreeStatusObserver.onActivityPaused(activity);
            View focusView = activity.getWindow().getDecorView().findFocus();
            if (focusView instanceof EditText) {
                Logger.d(TAG, "onActivityPaused, and focus view is EditText");
                editTextOnFocusChange(focusView);
            }
        }
    }

    @Override
    public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
        if (changedEvent.getStateType() == ViewStateChangedEvent.StateType.FOCUS_CHANGED) {
            View oldFocus = changedEvent.getOldFocus();
            if (oldFocus instanceof EditText) {
                Logger.d(TAG, "onViewStateChanged, and oldFocus view is EditText");
                editTextOnFocusChange(oldFocus);
            }
        }
    }

    void editTextOnFocusChange(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isEditTextChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: edittext change enable is false");
            return;
        }
        sendChangeEvent(view);
    }

    public void seekBarOnProgressChange(SeekBar seekBar) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isSeekbarChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: seekbar change enable is false");
            return;
        }
        sendChangeEvent(seekBar);
    }

    public void ratingBarOnRatingChange(View view, float rating) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isRatingBarChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: ratingbar change enable is false");
            return;
        }
        sendChangeEvent(view);
    }

    public void sliderOnStopTrackingTouch(Slider slider) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isSliderChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: slider value change enable is false");
            return;
        }
        sendChangeEvent(slider);
    }

    public void rangeSliderOnStopTrackingTouch(RangeSlider slider) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isSliderChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: rangeSlider value change enable is false");
            return;
        }
        sendChangeEvent(slider);
    }

    private void sendChangeEvent(View view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
        }

        if (autotrackConfig == null || !autotrackConfig.isAutotrack()) {
            Logger.d(TAG, "autotrack is not enabled");
            return;
        }

        if (view == null) {
            Logger.e(TAG, "viewOnChange:View is NULL");
            return;
        }
        viewNodeProvider.generateViewChangeEvent(view);
    }
}
