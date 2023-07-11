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

package com.growingio.android.sdk.autotrack.impression;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.view.OnViewStateChangedListener;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.view.ViewTreeStatusObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ImpressionProvider implements IActivityLifecycle, OnViewStateChangedListener {
    private static final String TAG = "ImpressionProvider";

    private static final int CHECK_IMPRESSION_ANTI_SHAKE_TIME = 500;

    private static final Map<Activity, List<ViewImpression>> ACTIVITY_SCOPE = new WeakHashMap<>();
    private final float mImpressionScale;
    private final Handler mUiHandler;
    private final Runnable mCheckImpressionRunnable = new Runnable() {
        @Override
        public void run() {
            checkImpression();
        }
    };

    private final ViewTreeStatusObserver mViewTreeStatusObserver;

    private static class SingleInstance {
        private static final ImpressionProvider INSTANCE = new ImpressionProvider();
    }

    private ImpressionProvider() {
        AutotrackConfig configuration = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        mImpressionScale = configuration.getImpressionScale();

        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        mViewTreeStatusObserver = new ViewTreeStatusObserver(this);
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public static ImpressionProvider get() {
        return SingleInstance.INSTANCE;
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (!ACTIVITY_SCOPE.containsKey(activity)) return;
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            mViewTreeStatusObserver.onActivityResumed(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            mViewTreeStatusObserver.onActivityPaused(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED) {
            ACTIVITY_SCOPE.remove(activity);
        }
    }

    @Override
    public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
        delayCheckImpression();
    }

    private void delayCheckImpression() {
        mUiHandler.removeCallbacks(mCheckImpressionRunnable);
        mUiHandler.postDelayed(mCheckImpressionRunnable, CHECK_IMPRESSION_ANTI_SHAKE_TIME);
    }

    private void checkImpression() {
        Activity activity = ActivityStateProvider.get().getResumedActivity();
        List<ViewImpression> viewImpressions = ACTIVITY_SCOPE.get(activity);
        if (activity == null || viewImpressions == null || viewImpressions.isEmpty()) {
            Logger.w(TAG, "ResumedActivity is NULL or This activity has nothing impression");
            return;
        }

        for (ViewImpression impression : viewImpressions) {
            View trackedView = impression.getTrackedView();
            if (trackedView == null) {
                continue;
            }
            boolean lastVisible = impression.isLastVisible();
            boolean currentVisible = isVisibility(trackedView);
            if (currentVisible && !lastVisible) {
                sendViewImpressionEvent(impression);
            }
            impression.setLastVisible(currentVisible);
        }

    }

    private boolean isVisibility(View view) {
        if (ViewAttributeUtil.viewVisibilityInParents(view)) {
            if (mImpressionScale <= 0) {
                return true;
            }

            Rect rect = new Rect();
            view.getLocalVisibleRect(rect);
            return rect.right * rect.bottom >= view.getMeasuredHeight() * view.getMeasuredWidth() * mImpressionScale;
        }
        return false;
    }

    private void sendViewImpressionEvent(ViewImpression impression) {
        View trackedView = impression.getTrackedView();
        if (trackedView == null) {
            return;
        }
        Logger.d(TAG, "find View from invisible to visible, send impression event");
        Page<?> page = PageProvider.get().findPage(trackedView);
        if (page == null) {
            Logger.w(TAG, "sendViewImpressionEvent trackedView Activity is NULL");
            return;
        }
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageLevelCustomEvent.Builder()
                        .setEventName(impression.getImpressionEventName())
                        .setAttributes(impression.getEventAttributes())
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
        );
    }

    public void trackViewImpression(View view, String impressionEventName, Map<String, String> attributes) {
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            return;
        }
        if (ViewAttributeUtil.isIgnoredView(view)) {
            Logger.w(TAG, "Current view is set to ignore");
            return;
        }
        Activity activity = findViewActivity(view);
        if (activity == null) {
            Logger.e(TAG, "View context activity is NULL");
            return;
        }

        List<ViewImpression> viewImpressions = ACTIVITY_SCOPE.get(activity);
        if (viewImpressions == null) {
            viewImpressions = new ArrayList<>();
            ACTIVITY_SCOPE.put(activity, viewImpressions);
        }
        for (int i = 0; i < viewImpressions.size(); i++) {
            if (viewImpressions.get(i).getTrackedView() == view) {
                viewImpressions.remove(i);
                break;
            }
        }

        Logger.d(TAG, "add view to impression list");
        viewImpressions.add(new ViewImpression(view, impressionEventName, attributes));
        mViewTreeStatusObserver.onActivityResumed(activity);
        delayCheckImpression();
    }

    public boolean hasTrackViewImpression(View trackedView) {
        if (trackedView == null) {
            return false;
        }
        Activity activity = findViewActivity(trackedView);
        if (activity == null) {
            Logger.e(TAG, "TrackedView context activity is NULL");
            return false;
        }

        List<ViewImpression> viewImpressions = ACTIVITY_SCOPE.get(activity);
        if (viewImpressions == null || viewImpressions.isEmpty()) {
            Logger.w(TAG, "ViewImpressions is NULL");
            return false;
        }
        return true;
    }

    public void stopTrackViewImpression(View trackedView) {
        if (trackedView == null) {
            return;
        }
        Activity activity = findViewActivity(trackedView);
        if (activity == null) {
            Logger.e(TAG, "TrackedView context activity is NULL");
            return;
        }

        List<ViewImpression> viewImpressions = ACTIVITY_SCOPE.get(activity);
        if (viewImpressions == null || viewImpressions.isEmpty()) {
            Logger.w(TAG, "ViewImpressions is NULL");
            return;
        }
        for (int i = 0; i < viewImpressions.size(); i++) {
            if (viewImpressions.get(i).getTrackedView() == trackedView) {
                viewImpressions.remove(i);
                Logger.d(TAG, "remove view from impression list");
                break;
            }
        }
        if (viewImpressions.isEmpty()) {
            mViewTreeStatusObserver.onActivityPaused(activity);
            ACTIVITY_SCOPE.remove(activity);
        }
    }

    @Nullable
    private Activity findViewActivity(View view) {
        Activity activity = ActivityUtil.findActivity(view);
        if (activity == null) {
            Logger.w(TAG, "View context activity is NULL");
            activity = ActivityStateProvider.get().getResumedActivity();
        }
        return activity;
    }
}
