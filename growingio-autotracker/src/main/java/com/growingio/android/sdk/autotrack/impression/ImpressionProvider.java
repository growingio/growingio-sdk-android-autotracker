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

import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.events.PageLevelCustomEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.view.OnViewStateChangedListener;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewStateChangedEvent;
import com.growingio.android.sdk.autotrack.view.ViewTreeStatusProvider;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ImpressionProvider implements OnViewStateChangedListener {
    private static final String TAG = "ImpressionProvider";

    private static final int CHECK_IMPRESSION_ANTI_SHAKE_TIME = 500;

    private static final Map<Activity, List<ViewImpression>> ACTIVITY_SCOPE = new WeakHashMap<>();
    private final float mImpressionScale;
    private final Handler mUiHandler;
    private volatile boolean mStarted = false;
    private final Runnable mCheckImpressionRunnable = new Runnable() {
        @Override
        public void run() {
            checkImpression();
        }
    };

    private static class SingleInstance {
        private static final ImpressionProvider INSTANCE = new ImpressionProvider();
    }

    private ImpressionProvider() {
        AutotrackConfiguration configuration = ConfigurationProvider.get().getConfiguration(AutotrackConfiguration.class);
        mImpressionScale = configuration.getImpressionScale();

        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public static ImpressionProvider get() {
        return SingleInstance.INSTANCE;
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
            Logger.e(TAG, "ResumedActivity is NULL or This activity has nothing impression");
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
        if (ViewHelper.viewVisibilityInParents(view)) {
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
        TrackMainThread.trackMain().postEventToTrackMain(new PageLevelCustomEvent.Builder());
        View trackedView = impression.getTrackedView();
        if (trackedView == null) {
            return;
        }

        Page<?> page = PageProvider.get().findPage(trackedView);
        if (page == null) {
            Logger.e(TAG, "sendViewImpressionEvent trackedView Activity is NULL");
            return;
        }
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageLevelCustomEvent.Builder()
                        .setEventName(impression.getImpressionEventName())
                        .setAttributes(impression.getEventAttributes())
                        .setPageName(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
        );
    }

    private void start() {
        if (mStarted) {
            Logger.e(TAG, "ImpressionProvider is running");
            return;
        }
        mStarted = true;
        ViewTreeStatusProvider.get().register(this);
    }

    public void trackViewImpression(View view, String impressionEventName, Map<String, String> attributes) {
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            return;
        }
        if (!mStarted) {
            start();
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

        viewImpressions.add(new ViewImpression(view, impressionEventName, attributes));
        delayCheckImpression();
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
            Logger.e(TAG, "ViewImpressions is NULL");
            return;
        }
        for (int i = 0; i < viewImpressions.size(); i++) {
            if (viewImpressions.get(i).getTrackedView() == trackedView) {
                viewImpressions.remove(i);
                break;
            }
        }

    }

    @Nullable
    private Activity findViewActivity(View view) {
        Activity activity = ActivityUtil.findActivity(view);
        if (activity == null) {
            Logger.e(TAG, "View context activity is NULL");
            activity = ActivityStateProvider.get().getResumedActivity();
        }
        return activity;
    }
}
