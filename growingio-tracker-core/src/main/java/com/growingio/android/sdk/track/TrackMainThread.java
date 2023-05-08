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
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventFilterInterceptor;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.helper.DefaultEventFilterInterceptor;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.listener.OnTrackMainInitSDKCallback;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.EventSender;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.middleware.EventHttpSender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * GrowingIO主线程
 */
public final class TrackMainThread extends ListenerContainer<OnTrackMainInitSDKCallback, Void> {
    private static final String TAG = "TrackMainThread";

    private static final int MSG_INIT_SDK = 1;
    private final Looper mMainLooper;
    private final Handler mMainHandler;
    private final EventSender mEventSender;
    private final CoreConfiguration coreConfiguration;

    private final List<EventBuildInterceptor> mEventBuildInterceptors = new ArrayList<>();

    private TrackMainThread() {
        this(ConfigurationProvider.core());
    }

    @VisibleForTesting
    TrackMainThread(CoreConfiguration configuration) {
        this.coreConfiguration = configuration;
        int uploadInterval = configuration.isDebugEnabled() ? 0 : configuration.getDataUploadInterval();
        mEventSender = new EventSender(new EventHttpSender(), uploadInterval, configuration.getCellularDataLimit());

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

    public Handler getMainHandler() {
        return mMainHandler;
    }

    @TrackThread
    void initSDK() {
        dispatchActions(null);
    }

    public void postEventToTrackMain(final BaseEvent.BaseBuilder<?> eventBuilder) {
        postActionToTrackMain(buildEventSendRunnable(eventBuilder));
    }

    /**
     * generate event send runnable
     *
     * @param eventBuilder the event whether to be send
     * @return runnable
     */
    public Runnable buildEventSendRunnable(final BaseEvent.BaseBuilder<?> eventBuilder) {
        return new Runnable() {
            @Override
            public void run() {
                if (eventBuilder == null) {
                    return;
                }

                if (coreConfiguration.isDataCollectionEnabled()) {
                    onGenerateGEvent(eventBuilder);
                }
            }
        };
    }

    public void postActionToTrackMain(Runnable runnable) {
        if (mMainHandler.getLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }

        mMainHandler.post(runnable);
    }

    @TrackThread
    void onGenerateGEvent(BaseEvent.BaseBuilder<?> gEvent) {
        dispatchEventWillBuild(gEvent);

        if (!filterEvent(gEvent)) return;
        gEvent.readPropertyInTrackThread();

        BaseEvent event = gEvent.build();
        dispatchEventDidBuild(event);

        saveEvent(event);
    }

    private EventFilterInterceptor defaultFilterInterceptor;

    EventFilterInterceptor getEventFilterInterceptor() {
        if (coreConfiguration.getEventFilterInterceptor() != null) {
            return coreConfiguration.getEventFilterInterceptor();
        } else {
            if (defaultFilterInterceptor == null) {
                defaultFilterInterceptor = new DefaultEventFilterInterceptor();
            }
            return defaultFilterInterceptor;
        }
    }

    @TrackThread
    boolean filterEvent(BaseEvent.BaseBuilder<?> eventBuilder) {
        EventFilterInterceptor eventFilterInterceptor = getEventFilterInterceptor();
        if (eventFilterInterceptor == null) return true;
        /*String eventGroup = "undefine";
        if (!eventFilterInterceptor.filterEventGroup(eventGroup)) {
            return false;
        }*/

        if (!eventFilterInterceptor.filterEventType(eventBuilder.getEventType())) {
            Logger.w(TAG, "filter [" + eventBuilder.getEventType() + "] event by type");
            return false;
        }

        String eventPath = getEventPath(eventBuilder);
        if (!TextUtils.isEmpty(eventPath) && !eventFilterInterceptor.filterEventPath(eventPath)) {
            Logger.w(TAG, "filter [" + eventBuilder.getEventType() + "] event by path=" + eventPath);
            return false;
        }

        String eventName = getEventName(eventBuilder);
        if (!TextUtils.isEmpty(eventName) && !eventFilterInterceptor.filterEventName(eventName)) {
            Logger.w(TAG, "filter [CUSTOM] event by name=" + eventName);
            return false;
        }

        Map<String, Boolean> filterFields = eventFilterInterceptor.filterEventField(eventBuilder.getEventType(), eventBuilder.getFilterMap());
        eventBuilder.filterFieldProperty(filterFields);

        return true;
    }

    String getEventName(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder instanceof CustomEvent.Builder) {
            return ((CustomEvent.Builder) eventBuilder).getEventName();
        }
        return null;
    }

    String getEventPath(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder instanceof PageEvent.Builder) {
            return ((PageEvent.Builder) eventBuilder).getPath();
        }
        if (eventBuilder instanceof ViewElementEvent.Builder) {
            return ((ViewElementEvent.Builder) eventBuilder).getPath();
        }
        if (eventBuilder instanceof PageLevelCustomEvent.Builder) {
            return ((PageLevelCustomEvent.Builder) eventBuilder).getPath();
        }
        return null;
    }


    public void removeEventBuildInterceptor(EventBuildInterceptor interceptor) {
        synchronized (mEventBuildInterceptors) {
            if (interceptor != null) {
                mEventBuildInterceptors.remove(interceptor);
            }
        }
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
        if (!PersistentDataProvider.get().isSendVisitAfterRefreshSessionId()) {
            // we should resend visitEvent when sessionId refreshed
            SessionProvider.get().generateVisit();
        }
        mEventSender.sendEvent(event);
    }

    private class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_INIT_SDK) {
                initSDK();
                return;
            }

            throw new IllegalStateException("Unexpected value: " + msg.what);
        }
    }

}
