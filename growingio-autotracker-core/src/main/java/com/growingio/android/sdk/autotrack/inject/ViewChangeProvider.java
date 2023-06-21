/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.view.OnViewStateChangedListener;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewNode;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.view.ViewTreeStatusObserver;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

public class ViewChangeProvider implements IActivityLifecycle, OnViewStateChangedListener {
    private static final String TAG = "ViewChangeProvider";

    public ViewChangeProvider() {
    }

    private ViewTreeStatusObserver mViewTreeStatusObserver;

    public void setup() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        mViewTreeStatusObserver = new ViewTreeStatusObserver(false, false, true, false, this,
                R.id.growing_tracker_monitoring_focus_change);
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (activity == null) {
            return;
        }
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            mViewTreeStatusObserver.onActivityResumed(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            mViewTreeStatusObserver.onActivityPaused(activity);
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

    public static void editTextOnFocusChange(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isEditTextChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: edittext change enable is false");
            return;
        }
        sendChangeEvent(view);
    }

    public static void seekBarOnProgressChange(SeekBar seekBar) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isSeekbarChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: seekbar change enable is false");
            return;
        }
        sendChangeEvent(seekBar);
    }

    public static void ratingBarOnRatingChange(View view, float rating) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isRatingBarChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: ratingbar change enable is false");
            return;
        }
        sendChangeEvent(view);
    }

    public static void sliderOnStopTrackingTouch(Slider slider) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isSliderChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: slider value change enable is false");
            return;
        }
        sendChangeEvent(slider);
    }

    public static void rangeSliderOnStopTrackingTouch(RangeSlider slider) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isSliderChangeEnabled()) {
            Logger.i(TAG, "AutotrackOptions: rangeSlider value change enable is false");
            return;
        }
        sendChangeEvent(slider);
    }

    private static void sendChangeEvent(View view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
        }

        ViewNode viewNode = ViewHelper.getChangeViewNode(view);
        if (viewNode == null) {
            Logger.e(TAG, "ViewNode is NULL with the View: " + view.getClass().getSimpleName());
            return;
        }

        Page<?> page = PageProvider.get().findPage(viewNode.getView());
        if (page == null) {
            Logger.w(TAG, "sendChangeEvent page Activity is NULL");
            return;
        }

        TrackMainThread.trackMain().postEventToTrackMain(
                new ViewElementEvent.Builder(AutotrackEventType.VIEW_CHANGE)
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setXpath(viewNode.getXPath())
                        .setIndex(viewNode.getIndex())
                        .setTextValue(viewNode.getViewContent())
        );
    }
}
