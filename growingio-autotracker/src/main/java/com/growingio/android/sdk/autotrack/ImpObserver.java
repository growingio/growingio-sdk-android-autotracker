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

package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.graphics.Rect;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.growingio.android.sdk.autotrack.util.Util;
import com.growingio.android.sdk.autotrack.util.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.util.ViewHelper;
import com.growingio.android.sdk.track.base.event.ViewTreeStatusChangeEvent;
import com.growingio.android.sdk.track.ListenerContainer;
import com.growingio.android.sdk.track.interfaces.IViewTreeStatus;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.utils.JsonUtil;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.ObjectUtils;
import com.growingio.android.sdk.track.utils.SysTrace;
import com.growingio.android.sdk.track.utils.TimerToggler;
import com.growingio.android.sdk.track.utils.WeakSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 用于监听并生成imp事件
 * <p>
 * 由OnGlobalLayoutListener触发, EventBus订阅有此类自行订阅与反订阅
 */

public class ImpObserver implements IViewTreeStatus {
    private static final String TAG = "GIO.Imp";

    private static final String GIO_CONTENT = "gio_v";

    TimerToggler mViewTreeChangeTimerToggler;
    @VisibleForTesting
    WeakHashMap<Activity, ActivityScope> mActivityScopes;

    @VisibleForTesting
    List<View> mTmpViewCache = new ArrayList<>();
    private Set<String> mTmpGlobalIds;
    private Rect mTmpRect;

    private void init() {
        if (mActivityScopes != null) {
            return;
        }
        mActivityScopes = new WeakHashMap<>();
        mViewTreeChangeTimerToggler = new TimerToggler.Builder(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "stampViewImp after resume or re draw, force check");
                onGlobalLayout(new ViewTreeStatusChangeEvent(ViewTreeStatusChangeEvent.StatusType.LayoutChanged));
            }
        }).delayTime(500).maxDelayTime(5000).firstTimeDelay(true).build();
        ListenerContainer.viewTreeStatusListeners().register(this);
    }

    public void markViewImpression(ImpressionConfig mark) {
        View view = mark.getView();
        if (view == null) {
            return;
        }

        Activity activity = ActivityUtil.findActivity(view.getContext());
        if (activity == null) {
            activity = ActivityStateProvider.ActivityStatePolicy.get().getForegroundActivity();
        }
        if (activity == null) {
            LogUtil.e(TAG, "can't find the activity of view: " + view);
            return;
        }
        LogUtil.d(TAG, "stampViewImp: ", mark.getEventName());
        init();
        ActivityScope scope = mActivityScopes.get(activity);
        if (scope == null) {
            scope = new ActivityScope(activity);
            mActivityScopes.put(activity, scope);
        }

        ImpEvent event = new ImpEvent();
        event.mMark = mark;
        event.mActivity = new WeakReference<>(activity);

        if (mark.getGlobalId() != null) {
            event = moveGlobalId(scope, view, mark, event);
            if (event == null) {
                LogUtil.d(TAG, "stampViewImp, and nothing changed, globalId: ", mark.getGlobalId());
                return;
            }
        } else if (scope.containView(view)) {
            ImpEvent impEvent = scope.getImpEvent(view);
            if (event.equals(impEvent)) {
                LogUtil.d(TAG, "stampViewImp, and nothing changed: ", mark.getEventName());
                impEvent.mMark = event.mMark;
                return;
            }
            stopStampViewImpInternal(scope, view);
        }
        ViewAttributeUtil.setImpMarked(view, true);
        scope.getFromDelay(mark.getDelayTimeMills()).addView(view, event, scope);
        checkAndSendViewTreeChange(activity);
    }

    private ImpEvent moveGlobalId(ActivityScope scope, View view, @NonNull ImpressionConfig mark, @NonNull ImpEvent impEvent) {
        if (!scope.mGlobalIdToImpEvent.containsKey(mark.getGlobalId())) {
            // globalId对应的元素未被记录
            scope.mGlobalIdToImpEvent.put(mark.getGlobalId(), impEvent);
            return impEvent;
        } else {
            ImpEvent globalImpEvent = scope.mGlobalIdToImpEvent.get(mark.getGlobalId());
            View oldView = globalImpEvent.mMark.getView();
            ImpEvent currentViewImpEvent = scope.getImpEvent(view);
            if (oldView != view) {
                // globalId对应不同的View
                if (currentViewImpEvent != null && currentViewImpEvent != globalImpEvent) {
                    scope.mNextPassInvisible.add(currentViewImpEvent);
                }
                stopStampViewImpInternal(scope, view);
                if (oldView == null) {
                    //TODO: 应该放在可见性判断那里进行检测 oldView被GC回收, 认为上次不可见
                    globalImpEvent.mLastVisible = false;
                }
            } else if (impEvent.equals(currentViewImpEvent)) {
                // globalId前后内容一致， 且 view相同
                return null;
            }
            globalImpEvent.mMark = mark;
            globalImpEvent.mActivity = impEvent.mActivity;
            return globalImpEvent;
        }
    }

    private void checkAndSendViewTreeChange(Activity activity) {
        Activity resumeActivity = ActivityStateProvider.ActivityStatePolicy.get().getResumedActivity();
        if (resumeActivity != null && activity == resumeActivity) {
            mViewTreeChangeTimerToggler.toggle();
        }
    }

    private ImpEvent stopStampViewImpInternal(ActivityScope scope, View view) {
        ViewAttributeUtil.setImpMarked(view, false);
        if (scope.mViewToTogglerWithViews.containsKey(view)) {
            TogglerWithViews togglerWithViews = scope.mViewToTogglerWithViews.get(view);
            ImpEvent result = togglerWithViews.getViewImpEvent(view);
            togglerWithViews.removeView(view, scope);
            return result;
        }
        return null;
    }


    public void stopStampViewImp(View view) {
        ActivityScope scope = findActivityScopeByView(view);
        if (scope == null) {
            return;
        }
        stopStampViewImpInternal(scope, view);
    }

    private void removeGlobalId(ActivityScope scope, ImpEvent impEvent) {
        if (impEvent != null && impEvent.mMark.getGlobalId() != null) {
            scope.mGlobalIdToImpEvent.remove(impEvent.mMark.getGlobalId());
        }
    }

    // only used by stopStampViewImp
    private ActivityScope findActivityScopeByView(View view) {
        if (mActivityScopes == null || view == null) {
            return null;
        }
        Activity activity = ActivityUtil.findActivity(view.getContext());
        if (activity != null) {
            return mActivityScopes.get(activity);
        }
        for (ActivityScope scope : mActivityScopes.values()) {
            if (scope.mViewToTogglerWithViews.containsKey(view)) {
                return scope;
            }
        }
        return null;
    }

    @Override
    public void onViewTreeStatusChanged(ViewTreeStatusChangeEvent action) {
        switch (action.getStatusType()) {
            case Draw:
                onGlobalDraw();
                break;
            case WindowFouchChanged:
                onGlobalWindowFocusChanged();
                break;
            default:
                onGlobalLayout(action);
        }
    }

    public void onGlobalWindowFocusChanged() {
        onGlobalLayout(null);
    }

    public void onGlobalLayout(ViewTreeStatusChangeEvent event) {
        Activity current = ActivityStateProvider.ActivityStatePolicy.get().getResumedActivity();
        if (current != null) {
            layoutActivity(current);
        }
    }

    public void onGlobalDraw() {
        if (mViewTreeChangeTimerToggler != null) {
            mViewTreeChangeTimerToggler.toggle();
        }
    }

    private void layoutActivity(Activity current) {
        if (mActivityScopes == null) {
            return;
        }
        ActivityScope scope = mActivityScopes.get(current);
        if (scope == null) {
            return;
        }
        for (TogglerWithViews toggler : scope.mTogglerWithViews) {
            toggler.toggle();
        }
    }

    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED) {
            mActivityScopes.remove(event.getActivity());
            if (mActivityScopes.isEmpty()) {
                ListenerContainer.viewTreeStatusListeners().unRegister(this);
                mViewTreeChangeTimerToggler.reset();
                mActivityScopes = null;
            }
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            ActivityScope scope = mActivityScopes.get(event.getActivity());
            if (scope != null) {
                for (View view : scope.mViewToTogglerWithViews.keySet()) {
                    ImpEvent impEvent = scope.getImpEvent(view);
                    if (impEvent != null) {
                        impEvent.mLastVisible = false;
                    }
                }
                layoutActivity(event.getActivity());
            }
        }
    }

    private void saveImpEvent(ImpEvent impEvent) {
        Map <String, String> variable = impEvent.mMark.getAttributes();
        if (impEvent.mMark.isCollectContent()) {
            String content = Util.getViewContent(impEvent.mMark.getView(), null);
            if (!TextUtils.isEmpty(content)) {
                if (variable == null) {
                    variable = new HashMap<>();
                }
                if (!variable.containsKey(GIO_CONTENT)) {
                    variable.put(GIO_CONTENT, content);
                }
            }
        }
        if (variable != null) {
            GrowingAutotracker.getInstance().trackCustomEvent(impEvent.mMark.getEventName(), variable);
        } else {
            GrowingAutotracker.getInstance().trackCustomEvent(impEvent.mMark.getEventName(), null);
        }
    }

    void checkImp(TogglerWithViews togglerWithViews, WeakHashMap<View, ImpEvent> impViews) {
        Activity current = ActivityStateProvider.ActivityStatePolicy.get().getResumedActivity();
        if (current == null || mActivityScopes == null || !mActivityScopes.containsKey(current)) {
            return;
        }
        ActivityScope scope = mActivityScopes.get(current);
        removeOutDateGlobalId(scope);
        if (impViews == null || impViews.isEmpty()) {
            scope.mTogglerWithViews.remove(togglerWithViews);
            return;
        }
        mViewTreeChangeTimerToggler.reset();
        LogUtil.d(TAG, "checkImp");
        mTmpViewCache.clear();
        for (View view : impViews.keySet()) {
            ImpEvent event = impViews.get(view);
            if (event == null) {
                // impossible
                continue;
            }
            boolean lastVisible = event.mLastVisible;
            boolean currentVisible = checkViewVisibility(event.mMark);
            if (event.mMark.getView() != view) {
                // current impEvent's view not current view, may be globalId changed
                mTmpViewCache.add(view);
                LogUtil.e(TAG, "event's view is not same with current view, maybe globalId changed");
                continue;
            }
            if (currentVisible && !lastVisible) {
                saveImpEvent(event);
            }
            event.mLastVisible = currentVisible;
        }
        for (ImpEvent impEvent : scope.mNextPassInvisible) {
            if (!scope.containView(impEvent.mMark.getView())) {
                impEvent.mLastVisible = false;
            }
        }
        scope.mNextPassInvisible.clear();
        for (View view : mTmpViewCache) {
            stopStampViewImpInternal(scope, view);
        }
        mTmpViewCache.clear();
    }

    boolean checkViewVisibility(ImpressionConfig mark) {
        View view = mark.getView();
        if (ViewHelper.viewVisibilityInParents(view)) {
            if (ImpressionConfig.getVisibleScale() == 0) {
                // 任意像素可见均被认为有效曝光
                return true;
            }

            if (mTmpRect == null) {
                mTmpRect = new Rect();
            }
            view.getLocalVisibleRect(mTmpRect);
            return mTmpRect.right * mTmpRect.bottom >= view.getMeasuredHeight() * view.getMeasuredWidth() * ImpressionConfig.getVisibleScale();
        }
        return false;
    }

    private void removeOutDateGlobalId(ActivityScope scope) {
        if (mTmpGlobalIds == null) {
            mTmpGlobalIds = new HashSet<>();
        }
        mTmpGlobalIds.clear();
        for (String globalId : scope.mGlobalIdToImpEvent.keySet()) {
            ImpEvent impEvent = scope.mGlobalIdToImpEvent.get(globalId);
            View currentView = impEvent.mMark.getView();
            if (currentView == null) {
                mTmpGlobalIds.add(globalId);
            } else {
                ImpEvent viewTargetImpEvent = scope.getImpEvent(currentView);
                if (viewTargetImpEvent != impEvent) {
                    mTmpGlobalIds.add(globalId);
                }
            }
        }
        if (mTmpGlobalIds.size() != 0) {
            for (String globalKey : mTmpGlobalIds) {
                scope.mGlobalIdToImpEvent.remove(globalKey);
            }
        }
    }

    static class TogglerWithViews implements Runnable {
        TimerToggler mTimerToggler;
        WeakHashMap<View, ImpEvent> mImpViews;
        long mDelayTime;
        ImpObserver mImpObserver;

        private TogglerWithViews(long delayTime) {
            mImpViews = new WeakHashMap<>();
            mTimerToggler = new TimerToggler.Builder(this)
                    .maxDelayTime(2000)
                    .delayTime(delayTime)
                    .build();
            this.mDelayTime = delayTime;
            mImpObserver = AutotrackAppState.impObserver();
        }

        public void addView(View view, ImpEvent impEvent, ActivityScope scope) {
            mImpViews.put(view, impEvent);
            scope.mViewToTogglerWithViews.put(view, this);
        }

        public void removeView(View view, ActivityScope scope) {
            if (mImpViews != null) {
                mImpViews.remove(view);
            }
            scope.mViewToTogglerWithViews.remove(view);
        }

        public ImpEvent getViewImpEvent(View view) {
            return mImpViews == null ? null : mImpViews.get(view);
        }

        public void toggle() {
            mTimerToggler.toggle();
        }

        @Override
        public void run() {
            try {
                SysTrace.beginSection("gio.imp");
                mImpObserver.checkImp(this, mImpViews);
            } finally {
                SysTrace.endSection();
            }
        }
    }

    private static class ActivityScope {
        final WeakReference<Activity> mActivity;
        final List<TogglerWithViews> mTogglerWithViews;
        final HashMap<String, ImpEvent> mGlobalIdToImpEvent;
        final WeakHashMap<View, TogglerWithViews> mViewToTogglerWithViews;
        final WeakSet<ImpEvent> mNextPassInvisible = new WeakSet<>();

        private ActivityScope(Activity activity) {
            this.mActivity = new WeakReference<>(activity);
            mTogglerWithViews = new ArrayList<>();
            mViewToTogglerWithViews = new WeakHashMap<>();
            mGlobalIdToImpEvent = new HashMap<>();
        }

        public boolean containView(View view) {
            return mViewToTogglerWithViews.containsKey(view);
        }

        public ImpEvent getImpEvent(View view) {
            TogglerWithViews togglerWithViews = mViewToTogglerWithViews.get(view);
            if (togglerWithViews != null) {
                return togglerWithViews.getViewImpEvent(view);
            }
            return null;
        }

        public TogglerWithViews getFromDelay(long delayTime) {
            for (TogglerWithViews togglerWithViews : mTogglerWithViews) {
                if (togglerWithViews.mDelayTime == delayTime) {
                    return togglerWithViews;
                }
            }
            TogglerWithViews withViews = new TogglerWithViews(delayTime);
            mTogglerWithViews.add(withViews);
            return withViews;
        }
    }

    static class ImpEvent {
        ImpressionConfig mMark;
        boolean mLastVisible;
        WeakReference<Activity> mActivity;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ImpEvent)) {
                return false;
            }
            ImpEvent other = (ImpEvent) obj;
            if (!ObjectUtils.equals(mMark.getEventName(), other.mMark.getEventName())
                    || !ObjectUtils.equals(mMark.getGlobalId(), other.mMark.getGlobalId())
                    || mMark.getDelayTimeMills() != other.mMark.getDelayTimeMills()
                    || !ObjectUtils.equals(mMark.getAttributes(), other.mMark.getAttributes())) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = mMark != null ? mMark.hashCode() : 0;
            result = 31 * result + (mLastVisible ? 1 : 0);
            result = 31 * result + (mActivity != null ? mActivity.hashCode() : 0);
            return result;
        }
    }
}
