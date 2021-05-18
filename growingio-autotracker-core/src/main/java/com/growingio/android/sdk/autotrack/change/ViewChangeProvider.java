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

package com.growingio.android.sdk.autotrack.change;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.track.view.OnViewStateChangedListener;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewNode;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.view.ViewTreeStatusProvider;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

public class ViewChangeProvider implements IActivityLifecycle, OnViewStateChangedListener {
    private static final String TAG = "ViewChangeProvider";


    public ViewChangeProvider() {
    }

    public void start() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        ViewTreeStatusProvider.get().register(this);
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            Activity activity = event.getActivity();
            if (activity == null) {
                return;
            }
            View focusView = activity.getWindow().getDecorView().findFocus();
            if (focusView instanceof EditText) {
                Logger.d(TAG, "onActivityPaused, and focus view is EditText");
                viewOnChange(focusView);
            }
        }
    }

    @Override
    public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
        if (changedEvent.getStateType() == ViewStateChangedEvent.StateType.FOCUS_CHANGED) {
            View oldFocus = changedEvent.getOldFocus();
            if (oldFocus instanceof EditText) {
                Logger.d(TAG, "onViewStateChanged, and oldFocus view is EditText");
                viewOnChange(oldFocus);
            }
        }
    }

    public static void viewOnChange(View view) {
        if (!Autotracker.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
        }

        ViewNode viewNode = ViewHelper.getChangeViewNode(view);
        if (viewNode != null) {
            sendChangeEvent(viewNode);
        } else {
            Logger.e(TAG, "ViewNode is NULL");
        }
    }

    private static void sendChangeEvent(ViewNode viewNode) {
        Page<?> page = PageProvider.get().findPage(viewNode.getView());
        if (page == null) {
            Logger.e(TAG, "sendChangeEvent page Activity is NULL");
            return;
        }
        TrackMainThread.trackMain().postEventToTrackMain(
                new ViewElementEvent.Builder()
                        .setEventType(AutotrackEventType.VIEW_CHANGE)
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setXpath(viewNode.getXPath())
                        .setIndex(viewNode.getIndex())
                        .setTextValue(viewNode.getViewContent())
        );
    }
}
