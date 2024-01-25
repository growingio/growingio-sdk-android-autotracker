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

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.R;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

/**
 * <p>
 *
 * @author cpacm 2023/5/5
 */
public abstract class ViewTreeStatusListener implements IActivityLifecycle, OnViewStateChangedListener, EventBuildInterceptor, TrackerLifecycleProvider {

    private final ViewTreeStatusObserver viewTreeStatusObserver;

    protected ActivityStateProvider activityStateProvider;
    protected EventBuilderProvider eventBuilderProvider;

    public ViewTreeStatusListener() {
        viewTreeStatusObserver = new ViewTreeStatusObserver(this, R.id.growing_tracker_monitoring_view_tree_listener);
    }

    @Override
    public void setup(TrackerContext context) {
        activityStateProvider = context.getActivityStateProvider();
        eventBuilderProvider = context.getEventBuilderProvider();
    }

    @Override
    public void shutdown() {
        unRegister();
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            viewTreeStatusObserver.onActivityResumed(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            viewTreeStatusObserver.onActivityPaused(activity);
        }
    }

    public void register() {
        activityStateProvider.registerActivityLifecycleListener(this);
        registerResumedActivity();
        eventBuilderProvider.addEventBuildInterceptor(this);
    }

    public void unRegister() {
        activityStateProvider.unregisterActivityLifecycleListener(this);
        Activity activity = activityStateProvider.getResumedActivity();
        if (activity != null) {
            viewTreeStatusObserver.onActivityPaused(activity);
        }
        eventBuilderProvider.removeEventBuildInterceptor(this);
    }

    private void registerResumedActivity() {
        Activity activity = activityStateProvider.getResumedActivity();
        if (activity != null && !activity.isDestroyed()) {
            viewTreeStatusObserver.onActivityResumed(activity);
        }
    }

    @Override
    public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
    }

    @Override
    public void eventDidBuild(GEvent event) {
        if (enableManualState()) {
            viewTreeStatusObserver.sendManualStateChangedEvent();
        }
    }

    public boolean enableManualState() {
        return true;
    }
}
