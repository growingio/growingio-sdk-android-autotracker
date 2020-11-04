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

package com.growingio.android.sdk.track;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.interfaces.OnTrackMainInitSDKCallback;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.EventSender;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.variation.EventHttpSender;
import com.growingio.android.sdk.track.variation.TrackEventJsonMarshaller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GrowingIO主线程
 */
public final class TrackMainThread extends ListenerContainer<OnTrackMainInitSDKCallback, Void> {
    private static final String TAG = "TrackMainThread";

    private static final int MSG_INIT_SDK = 1;
    private static final int MSG_POST_EVENT = 2;
    private final Looper mMainLooper;
    private final Handler mMainHandler;
    private final EventSender mEventSender;

    private final List<EventBuildInterceptor> mEventBuildInterceptors = new ArrayList<>();

    private TrackMainThread() {
        TrackConfiguration configuration = ConfigurationProvider.get().getTrackConfiguration();
        int uploadInterval = configuration.isDebugEnabled() ? 0 : configuration.getDataUploadInterval();
        mEventSender = new EventSender(ContextProvider.getApplicationContext(), new EventHttpSender(new TrackEventJsonMarshaller()), uploadInterval, configuration.getCellularDataLimit());

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mMainLooper = handlerThread.getLooper();
        mMainHandler = new H(mMainLooper);
        mMainHandler.sendEmptyMessage(MSG_INIT_SDK);

    }

    @Override
    protected void singleAction(OnTrackMainInitSDKCallback listener, Void action) {
        listener.onTrackMainInitSDK();
    }

    private static class SingleInstance {
        private static final TrackMainThread INSTANCE = new TrackMainThread();
    }

    public static TrackMainThread trackMain() {
        return SingleInstance.INSTANCE;
    }

    public Looper getMainLooper() {
        return mMainLooper;
    }

    @TrackThread
    void initSDK() {
        mEventSender.removeOverdueEvents();
        dispatchActions(null);
    }

    public void postEventToTrackMain(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder == null) {
            return;
        }
        if (ConfigurationProvider.get().isDataCollectionEnabled()) {
            if (!SessionProvider.get().createdSession()) {
                SessionProvider.get().forceReissueVisit();
            }

            Message msg = mMainHandler.obtainMessage(MSG_POST_EVENT);
            msg.obj = eventBuilder;
            mMainHandler.sendMessage(msg);
        }
    }

    public void postActionToTrackMain(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    @TrackThread
    void onGenerateGEvent(BaseEvent.BaseBuilder<?> gEvent) {
        gEvent.readPropertyInTrackThread();
        dispatchEventWillBuild(gEvent);
        BaseEvent event = gEvent.build();
        dispatchEventDidBuild(event);
        saveEvent(event);
    }

    public void addEventBuildInterceptor(EventBuildInterceptor interceptor) {
        synchronized (mEventBuildInterceptors) {
            boolean needsAdd = true;
            Iterator<EventBuildInterceptor> refIter = mEventBuildInterceptors.iterator();
            while (refIter.hasNext()) {
                EventBuildInterceptor storedInterceptor = refIter.next();
                if (null == storedInterceptor) {
                    refIter.remove();
                } else if (storedInterceptor == interceptor) {
                    needsAdd = false;
                }
            }
            if (needsAdd) {
                mEventBuildInterceptors.add(interceptor);
            }
        }
    }

    private void dispatchEventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
        synchronized (mEventBuildInterceptors) {
            Iterator<EventBuildInterceptor> refIter = mEventBuildInterceptors.iterator();
            while (refIter.hasNext()) {
                EventBuildInterceptor interceptor = refIter.next();
                if (null == interceptor) {
                    refIter.remove();
                } else {
                    try {
                        interceptor.eventWillBuild(eventBuilder);
                    } catch (Exception e) {
                        Logger.e(TAG, e);
                    }
                }
            }
        }
    }

    private void dispatchEventDidBuild(GEvent event) {
        synchronized (mEventBuildInterceptors) {
            Iterator<EventBuildInterceptor> refIter = mEventBuildInterceptors.iterator();
            while (refIter.hasNext()) {
                EventBuildInterceptor interceptor = refIter.next();
                if (null == interceptor) {
                    refIter.remove();
                } else {
                    try {
                        interceptor.eventDidBuild(event);
                    } catch (Exception e) {
                        Logger.e(TAG, e);
                    }
                }
            }
        }
    }

    @TrackThread
    private void saveEvent(GEvent event) {
        if (event instanceof BaseEvent) {
            Logger.printJson(TAG, "save: event, type is " + event.getEventType(), ((BaseEvent) event).toJSONObject().toString());
        }
        mEventSender.sendEvent(event);
    }

    private class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_INIT_SDK:
                    initSDK();
                    break;
                case MSG_POST_EVENT: {
                    BaseEvent.BaseBuilder<?> eventBuilder = (BaseEvent.BaseBuilder<?>) msg.obj;
                    onGenerateGEvent(eventBuilder);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    }

}
