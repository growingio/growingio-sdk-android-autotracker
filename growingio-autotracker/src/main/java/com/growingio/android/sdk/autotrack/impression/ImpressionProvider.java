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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ImpressionProvider implements IActivityLifecycle, OnViewStateChangedListener {
    private static final String TAG = "ImpressionProvider";

    private static final int CHECK_IMPRESSION_ANTI_SHAKE_TIME = 500;

    private static final Map<Activity, List<ViewImpression>> ACTIVITY_SCOPE = new WeakHashMap<>();
    private final DeprecatedViewStateObserver mViewStateObserver;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mViewStateObserver = new ViewStateObserver(this);
        } else {
            mViewStateObserver = new DeprecatedViewStateObserver(this);
        }
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
            LogUtil.e(TAG, "ResumedActivity is NULL or This activity has nothing impression");
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
        GrowingAutotracker.getInstance().trackCustomEvent(impression.getImpressionEventName(), impression.getEventAttributes());
    }

    private void start() {
        if (mStarted) {
            LogUtil.e(TAG, "ImpressionProvider is running");
            return;
        }
        mStarted = true;
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
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
            LogUtil.e(TAG, "View context activity is NULL");
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
            LogUtil.e(TAG, "TrackedView context activity is NULL");
            return;
        }

        List<ViewImpression> viewImpressions = ACTIVITY_SCOPE.get(activity);
        if (viewImpressions == null || viewImpressions.isEmpty()) {
            LogUtil.e(TAG, "ViewImpressions is NULL");
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
        Activity activity = ActivityUtil.findActivity(view.getContext());
        if (activity == null) {
            LogUtil.e(TAG, "View context activity is NULL");
            activity = ActivityStateProvider.get().getResumedActivity();
        }
        return activity;
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            monitorViewTreeChange(activity.getWindow().getDecorView());
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            unRegisterViewTreeChange(activity.getWindow().getDecorView());
        }
    }

    private void unRegisterViewTreeChange(View root) {
        if (ViewAttributeUtil.isMonitoringViewTree(root)) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(mViewStateObserver);
            root.getViewTreeObserver().removeOnGlobalFocusChangeListener(mViewStateObserver);
            root.getViewTreeObserver().removeOnScrollChangedListener(mViewStateObserver);
            root.getViewTreeObserver().removeOnDrawListener(mViewStateObserver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().removeOnWindowFocusChangeListener((ViewTreeObserver.OnWindowFocusChangeListener) mViewStateObserver);
            }

            ViewAttributeUtil.setMonitoringViewTreeEnabled(root, false);
        }
    }

    private void monitorViewTreeChange(View root) {
        if (!ViewAttributeUtil.isMonitoringViewTree(root)) {
            root.getViewTreeObserver().addOnGlobalLayoutListener(mViewStateObserver);
            root.getViewTreeObserver().addOnScrollChangedListener(mViewStateObserver);
            root.getViewTreeObserver().addOnGlobalFocusChangeListener(mViewStateObserver);
            root.getViewTreeObserver().addOnDrawListener(mViewStateObserver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.getViewTreeObserver().addOnWindowFocusChangeListener((ViewTreeObserver.OnWindowFocusChangeListener) mViewStateObserver);
            }

            ViewAttributeUtil.setMonitoringViewTreeEnabled(root, true);
        }
    }
}
