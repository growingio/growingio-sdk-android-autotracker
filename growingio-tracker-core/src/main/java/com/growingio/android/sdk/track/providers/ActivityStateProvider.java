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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

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
    private EventSenderProvider eventSenderProvider;

    private WeakReference<Application> applicationWeakReference;

    private ConnectivityManager.NetworkCallback networkCallback;
    private BroadcastReceiver networkReceiver;

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
        eventSenderProvider = context.getProvider(EventSenderProvider.class);
    }

    public void listenNetworkChange() {
        Application application = applicationWeakReference.get();
        if (application == null) {
            Logger.e(TAG, "Application is null, can't register network callback.");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNetworkCallback();
        } else {
            registerNetworkReceiver(application);
        }
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    private void registerNetworkCallback() {
        Application application = applicationWeakReference.get();
        if (application == null) {
            Logger.e(TAG, "Application is null, can't register network callback.");
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Logger.e(TAG, "ConnectivityManager is null, can't register network callback.");
            return;
        }

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        this.networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Logger.i(TAG, "Network is available, flush messages.");
                if (eventSenderProvider != null) eventSenderProvider.flush();
            }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    @SuppressWarnings("deprecation")
    private void registerNetworkReceiver(Application application) {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm == null) return;
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnected()) {
                        Logger.i(TAG, "Network is available, flush messages.");
                        if (eventSenderProvider != null) eventSenderProvider.flush();
                    }
                }
            }
        };
        application.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void shutdown() {
        if (applicationWeakReference != null) {
            Application application = applicationWeakReference.get();
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (networkCallback != null) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(networkCallback);
                        }
                    }
                } else {
                    if (networkReceiver != null) {
                        application.unregisterReceiver(networkReceiver);
                    }
                }
            }
        }
    }
}