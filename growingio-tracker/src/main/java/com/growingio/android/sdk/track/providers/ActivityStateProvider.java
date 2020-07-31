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

package com.growingio.android.sdk.track.providers;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.SysTrace;

import java.lang.ref.WeakReference;

public interface ActivityStateProvider {

    @AnyThread
    Activity getResumedActivity();

    @AnyThread
    Activity getForegroundActivity();

    @AnyThread
    int getCurrentRootWindowsHashCode();

    void registerActivityLifecycleListener(IActivityLifecycle lifecycle);

    void unregisterActivityLifecycleListener(IActivityLifecycle lifecycle);

    class ActivityStatePolicy extends ListenerContainer<IActivityLifecycle, ActivityLifecycleEvent> implements ActivityStateProvider, Application.ActivityLifecycleCallbacks {

        private WeakReference<Activity> mResumeActivity = new WeakReference<>(null);
        private WeakReference<Activity> mForegroundActivity = new WeakReference<>(null);
        private int mCurrentRootWindowsHashCode = -1;

        private ActivityStatePolicy() {
        }

        public static ActivityStateProvider get() {
            return GIOProviders.provider(ActivityStateProvider.class, new GIOProviders.DefaultCallback<ActivityStateProvider>() {
                @Override
                public ActivityStateProvider value() {
                    return new ActivityStatePolicy();
                }
            });
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

        @Override
        public synchronized Activity getResumedActivity() {
            return mResumeActivity.get();
        }

        @Override
        public synchronized Activity getForegroundActivity() {
            return mForegroundActivity.get();
        }

        private synchronized void setForegroundActivity(Activity activity) {
            mForegroundActivity = new WeakReference<>(activity);
        }

        @Override
        public int getCurrentRootWindowsHashCode() {
            if (mCurrentRootWindowsHashCode == -1
                    && mForegroundActivity != null && mForegroundActivity.get() != null) {
                //该时间点， 用户理论上setContentView已经结束
                mCurrentRootWindowsHashCode = mForegroundActivity.get().getWindow().getDecorView().hashCode();
            }
            return mCurrentRootWindowsHashCode;
        }

        @Override
        public void registerActivityLifecycleListener(IActivityLifecycle lifecycle) {
            register(lifecycle);
        }

        @Override
        public void unregisterActivityLifecycleListener(IActivityLifecycle lifecycle) {
            unregister(lifecycle);
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            SysTrace.beginSection("gio.ActivityOnCreate");
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnCreatedEvent(activity, savedInstanceState));
            SysTrace.endSection();
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            SysTrace.beginSection("gio.onActivityStart");
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnStartedEvent(activity));
            SysTrace.endSection();
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            SysTrace.beginSection("gio.onActivityResumed");
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnResumedEvent(activity));
            SysTrace.endSection();
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            SysTrace.beginSection("gio.onActivityPaused");
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnPausedEvent(activity));
            SysTrace.endSection();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnStoppedEvent(activity));
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnSaveInstanceStateEvent(activity, outState));
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            dispatchActivityLifecycle(ActivityLifecycleEvent.createOnDestroyedEvent(activity));
        }

        @Override
        protected void singleAction(IActivityLifecycle listener, ActivityLifecycleEvent action) {
            listener.onActivityLifecycle(action);
        }
    }
}
