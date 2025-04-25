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
package com.growingio.android.sdk.track.providers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.SysTrace;

import java.lang.ref.WeakReference;

public class ActivityStateProvider extends ListenerContainer<IActivityLifecycle, ActivityLifecycleEvent> implements Application.ActivityLifecycleCallbacks, TrackerLifecycleProvider {

    private static final String TAG = "ActivityStateProvider";
    private WeakReference<Activity> mResumeActivity = new WeakReference<>(null);
    private WeakReference<Activity> mForegroundActivity = new WeakReference<>(null);
    private ConfigurationProvider configurationProvider;

    private WeakReference<Application> applicationWeakReference;

    ActivityStateProvider(Context context) {
        if (context instanceof Application) {
            Application application = (Application) context;
            applicationWeakReference = new WeakReference<>(application);
            application.registerActivityLifecycleCallbacks(this);
        } else if (context instanceof Activity) {
            Activity activity = (Activity) context;
            mForegroundActivity = new WeakReference<>(activity);
            applicationWeakReference = new WeakReference<>(activity.getApplication());
            activity.getApplication().registerActivityLifecycleCallbacks(this);
        }/* else {
            // inaccessible
        }*/
    }

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
    }

    public void makeupActivityLifecycle() {
        Activity activity = mForegroundActivity.get();
        if (activity != null) {
            ActivityLifecycleEvent.EVENT_TYPE state = ActivityUtil.judgeContextState(activity);
            if (state != null) {
                Logger.i(TAG, "initSdk with Activity, makeup ActivityLifecycle before current state:" + state.name());
                if (state.compareTo(ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED) >= 0) {
                    onActivityCreated(activity, null);
                }
                if (state.compareTo(ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) >= 0) {
                    onActivityStarted(activity);
                }
                if (state.compareTo(ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) >= 0) {
                    onActivityResumed(activity);
                }
            } else if (!activity.isDestroyed()) {
                setResumeActivity(activity);
            }
        }
    }

    private void dispatchActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            setResumeActivity(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            setResumeActivity(null);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED) {
            setForegroundActivity(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {

        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED) {
            // 仅仅做个保护逻辑
            if (activity == getResumedActivity()) {
                setResumeActivity(null);
            }
            if (activity == getForegroundActivity()) {
                setForegroundActivity(null);
            }
        }
        dispatchActions(event);
    }

    private synchronized void setResumeActivity(Activity activity) {
        mResumeActivity = new WeakReference<>(activity);
        if (activity != null) {
            setForegroundActivity(activity);
        }
    }

    public synchronized Activity getResumedActivity() {
        return mResumeActivity.get();
    }

    public synchronized Activity getForegroundActivity() {
        return mForegroundActivity.get();
    }

    private synchronized void setForegroundActivity(Activity activity) {
        mForegroundActivity = new WeakReference<>(activity);
    }

    public void registerActivityLifecycleListener(IActivityLifecycle lifecycle) {
        register(lifecycle);
    }

    public void unregisterActivityLifecycleListener(IActivityLifecycle lifecycle) {
        unregister(lifecycle);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        boolean debug = configurationProvider.core().isDebugEnabled();
        SysTrace.beginSection("gio.ActivityOnCreate", debug);
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnCreatedEvent(activity, savedInstanceState));
        SysTrace.endSection(debug);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        boolean debug = configurationProvider.core().isDebugEnabled();
        SysTrace.beginSection("gio.onActivityStart", debug);
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnStartedEvent(activity));
        SysTrace.endSection(debug);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        boolean debug = configurationProvider.core().isDebugEnabled();
        SysTrace.beginSection("gio.onActivityResumed", debug);
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnResumedEvent(activity));
        SysTrace.endSection(debug);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        boolean debug = configurationProvider.core().isDebugEnabled();
        SysTrace.beginSection("gio.onActivityPaused", debug);
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnPausedEvent(activity));
        SysTrace.endSection(debug);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnStoppedEvent(activity));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnSaveInstanceStateEvent(activity, outState));
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnDestroyedEvent(activity));
    }

    public void onActivityNewIntent(Activity activity, Intent intent) {
        dispatchActivityLifecycle(ActivityLifecycleEvent.createOnNewIntentEvent(activity, intent));
    }


    @Override
    protected void singleAction(IActivityLifecycle listener, ActivityLifecycleEvent action) {
        listener.onActivityLifecycle(action);
    }

    @Override
    public void shutdown() {
        if (applicationWeakReference != null) {
            Application application = applicationWeakReference.get();
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(this);
            }
        }
    }
}