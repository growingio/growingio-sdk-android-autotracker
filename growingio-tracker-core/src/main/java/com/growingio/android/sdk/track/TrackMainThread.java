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
package com.growingio.android.sdk.track;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.PersistentDataProvider;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.EventSender;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.middleware.EventHttpSender;

/**
 * GrowingIO主线程
 */
public final class TrackMainThread {
    private static final String TAG = "TrackMainThread";

    private final CircularFifoQueue<BaseEvent.BaseBuilder<?>> caches;

    private final Handler mainHandler;
    private final Handler uiHandler;
    private EventSender eventSender;
    private CoreConfiguration coreConfiguration;
    private EventBuilderProvider eventBuilderProvider;
    private PersistentDataProvider persistentDataProvider;
    private SessionProvider sessionProvider;

    private ActivityStateProvider activityStateProvider;
    private Context context;

    private TrackMainThread() {
        caches = new CircularFifoQueue<>(200);
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mainHandler = new Handler(handlerThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public void setupWithContext(TrackerContext context) {
        this.context = context.getBaseContext();
        this.coreConfiguration = context.getConfigurationProvider().core();
        this.eventBuilderProvider = context.getEventBuilderProvider();
        this.persistentDataProvider = context.getProvider(PersistentDataProvider.class);
        this.sessionProvider = context.getProvider(SessionProvider.class);
        this.activityStateProvider = context.getActivityStateProvider();
        int uploadInterval = coreConfiguration.isDebugEnabled() ? 0 : coreConfiguration.getDataUploadInterval();
        eventSender = new EventSender(
                this.context,
                context.getRegistry(),
                new EventHttpSender(context),
                uploadInterval,
                coreConfiguration.getCellularDataLimit());
    }

    public void shutdown() {
        caches.clear();
        this.context = null;
        this.coreConfiguration = null;
        this.eventBuilderProvider = null;
        this.persistentDataProvider = null;
        this.sessionProvider = null;
        this.activityStateProvider = null;
        this.eventSender = null;
    }

    private static class SingleInstance {
        private static final TrackMainThread INSTANCE = new TrackMainThread();
    }

    public static TrackMainThread trackMain() {
        return SingleInstance.INSTANCE;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    /**
     * send events in the current thread
     */
    public void sendEventSync(final BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder == null) {
            return;
        }
        if (coreConfiguration != null && coreConfiguration.isDataCollectionEnabled()) {
            onGenerateGEvent(eventBuilder);
        }
    }

    public void postEventToTrackMain(final BaseEvent.BaseBuilder<?> eventBuilder) {
        postActionToTrackMain(() -> {
            if (eventBuilder == null) {
                return;
            }

            if (coreConfiguration != null && coreConfiguration.isDataCollectionEnabled()) {
                onGenerateGEvent(eventBuilder);
            }
        });
    }

    public void releaseCaches() {
        if (!caches.isEmpty() && coreConfiguration != null && coreConfiguration.isDataCollectionEnabled()) {
            for (BaseEvent.BaseBuilder<?> eventBuilder : caches) {
                TrackMainThread.trackMain().postEventToTrackMain(eventBuilder);
            }
            Logger.d(TAG, "release cache events after sdk init: count-" + caches.size());
            caches.clear();
        }
    }

    public void cacheEventToTrackMain(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder == null) return;
        if (coreConfiguration == null || !coreConfiguration.isDataCollectionEnabled()) {
            Logger.w(TAG, "cache event before sdk init: " + eventBuilder.getEventType());
            caches.add(eventBuilder);
        } else {
            postEventToTrackMain(eventBuilder);
        }
    }

    /**
     * cache events in the current thread
     */
    public void cacheEventSync(final BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder == null) return;
        if (coreConfiguration == null || !coreConfiguration.isDataCollectionEnabled()) {
            Logger.w(TAG, "cache event before sdk init: " + eventBuilder.getEventType());
            caches.add(eventBuilder);
        } else {
            sendEventSync(eventBuilder);
        }
    }

    public void postActionToTrackMain(Runnable runnable) {
        if (mainHandler.getLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }
        mainHandler.post(runnable);
    }

    private void onGenerateGEvent(BaseEvent.BaseBuilder<?> gEvent) {
        BaseEvent event = eventBuilderProvider.onGenerateGEvent(gEvent);
        if (event != null) saveEvent(event);
    }

    @TrackThread
    private void saveEvent(GEvent event) {
        if (event instanceof BaseEvent) {
            Logger.printJson(TAG, "save: event, type is " + event.getEventType(), EventBuilderProvider.toJson((BaseEvent) event).toString());
        }
        if (persistentDataProvider != null && !persistentDataProvider.isSendVisitAfterRefreshSessionId()) {
            // we should resend visitEvent when sessionId refreshed
            sessionProvider.generateVisit();
        }
        if (eventSender != null) eventSender.sendEvent(event);
    }

    public synchronized Activity getForegroundActivity() {
        if (activityStateProvider == null) return null;
        return activityStateProvider.getForegroundActivity();
    }

    public Context getContext() {
        return context;
    }

    public void runOnUiThread(Runnable r) {
        if (runningOnUiThread()) {
            r.run();
        } else {
            uiHandler.post(r);
        }
    }

    public boolean runningOnUiThread() {
        return uiHandler.getLooper() == Looper.myLooper();
    }

    public void postOnUiThreadDelayed(Runnable task, long delayMillis) {
        uiHandler.postDelayed(task, delayMillis);
    }

    public void removeOnUiThreadCallbacks(Runnable task) {
        uiHandler.removeCallbacks(task);
    }
}
