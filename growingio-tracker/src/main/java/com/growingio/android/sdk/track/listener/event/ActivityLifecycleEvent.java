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

package com.growingio.android.sdk.track.listener.event;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public class ActivityLifecycleEvent {

    public final EVENT_TYPE eventType;
    private WeakReference<Activity> mActivityWeakReference;
    private WeakReference<Bundle> mBundleWeakReference;
    private WeakReference<Intent> mIntentWeakReference;


    private ActivityLifecycleEvent(Activity activity, EVENT_TYPE eventType) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        this.eventType = eventType;
    }

    private ActivityLifecycleEvent(Activity activity, EVENT_TYPE eventType, Bundle outState) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        this.mBundleWeakReference = new WeakReference<>(outState);
        this.eventType = eventType;
    }

    private ActivityLifecycleEvent(Activity activity, EVENT_TYPE eventType, Intent intent) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        this.mIntentWeakReference = new WeakReference<>(intent);
        this.eventType = eventType;
    }

    public static ActivityLifecycleEvent createOnCreatedEvent(Activity activity, Bundle outState) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_CREATED, outState);
    }

    public static ActivityLifecycleEvent createOnStartedEvent(Activity activity) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_STARTED);
    }

    public static ActivityLifecycleEvent createOnResumedEvent(Activity activity) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_RESUMED);
    }

    public static ActivityLifecycleEvent createOnNewIntentEvent(Activity activity, Intent intent) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_NEW_INTENT, intent);
    }

    public static ActivityLifecycleEvent createOnPausedEvent(Activity activity) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_PAUSED);
    }

    public static ActivityLifecycleEvent createOnSaveInstanceStateEvent(Activity activity, Bundle outState) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_SAVE_INSTANCE_STATE, outState);
    }

    public static ActivityLifecycleEvent createOnStoppedEvent(Activity activity) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_STOPPED);
    }

    public static ActivityLifecycleEvent createOnDestroyedEvent(Activity activity) {
        return new ActivityLifecycleEvent(activity, EVENT_TYPE.ON_DESTROYED);
    }

    public Activity getActivity() {
        if (mActivityWeakReference != null)
            return mActivityWeakReference.get();
        else {
            return null;
        }
    }

    public Bundle getBundle() {
        if (mBundleWeakReference != null)
            return mBundleWeakReference.get();
        else {
            return null;
        }
    }

    public Intent getIntent() {
        if (mIntentWeakReference != null)
            if (mIntentWeakReference.get() != null) {
                return mIntentWeakReference.get();
            }
        Activity activity = getActivity();
        if (activity != null) {
            return activity.getIntent();
        }
        return null;
    }

    public enum EVENT_TYPE {
        ON_CREATED,
        ON_STARTED,
        ON_RESUMED,
        ON_NEW_INTENT,
        ON_PAUSED,
        ON_SAVE_INSTANCE_STATE,
        ON_STOPPED,
        ON_DESTROYED
    }
}
