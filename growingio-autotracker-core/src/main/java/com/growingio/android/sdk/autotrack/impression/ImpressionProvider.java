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
package com.growingio.android.sdk.autotrack.impression;

import android.app.Activity;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;
import com.growingio.android.sdk.track.view.OnViewStateChangedListener;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;
import com.growingio.android.sdk.track.view.ViewTreeStatusObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImpressionProvider implements IActivityLifecycle, OnViewStateChangedListener, TrackerLifecycleProvider {
    private static final String TAG = "ImpressionProvider";

    private static final long DEFAULT_CHECK_INTERVAL = 500L;

    /** 复检时点比停留时长多留一点余量，避免时钟精度导致刚好差一丁点而空跑一轮 */
    private static final long RECHECK_SLACK = 10L;

    /** 可见面积占比的比较容差，只用于吸收浮点误差 */
    private static final float SCALE_TOLERANCE = 1e-4f;

    private static final int MAX_TRACKED_IDENTIFIERS = 10000;

    private final Map<Activity, List<ViewImpression>> activityScope = new WeakHashMap<>();

    /** 不可重复曝光的元素标识，按插入顺序淘汰 */
    private final Set<String> trackedIdentifiers = new LinkedHashSet<>();
    private boolean trackedIdentifiersOverflowWarned = false;

    private final List<ImpressionListener> impressionListeners = new CopyOnWriteArrayList<>();

    private final ViewTreeStatusObserver viewTreeStatusObserver;
    private ActivityStateProvider activityStateProvider;

    private boolean impressionEnabled = true;
    private ImpressionConfig globalImpressionConfig = new ImpressionConfig();
    /** 检测节流间隔，单位毫秒。检测节流配置为内部预留，待后续迭代再公开 */
    private long checkInterval = DEFAULT_CHECK_INTERVAL;

    private long lastCheckTime = 0L;
    private boolean trailingCheckScheduled = false;

    private final Runnable checkImpressionRunnable = new Runnable() {
        @Override
        public void run() {
            trailingCheckScheduled = false;
            checkImpression();
        }
    };

    public ImpressionProvider() {
        viewTreeStatusObserver = new ViewTreeStatusObserver(this);
    }

    @Override
    public void setup(TrackerContext context) {
        activityStateProvider = context.getActivityStateProvider();

        AutotrackConfig autotrackConfig = context.getConfigurationProvider().getConfiguration(AutotrackConfig.class);
        if (autotrackConfig == null || !autotrackConfig.isAutotrack()) {
            impressionEnabled = false;
            Logger.i(TAG, "autotrack is disabled, impression collection won't work");
            return;
        }

        impressionEnabled = autotrackConfig.isImpressionEnabled();
        globalImpressionConfig = autotrackConfig.getImpressionConfig().copy();

        if (!impressionEnabled) {
            Logger.i(TAG, "impression collection is disabled");
            return;
        }
        activityStateProvider.registerActivityLifecycleListener(this);
    }

    @Override
    public void shutdown() {
        if (activityStateProvider != null) {
            activityStateProvider.unregisterActivityLifecycleListener(this);
        }
        TrackMainThread.trackMain().removeOnUiThreadCallbacks(checkImpressionRunnable);
        trailingCheckScheduled = false;
        activityScope.clear();
        trackedIdentifiers.clear();
        impressionListeners.clear();
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        List<ViewImpression> viewImpressions = activityScope.get(activity);
        if (viewImpressions == null) return;
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            viewTreeStatusObserver.onActivityResumed(activity);
            scheduleCheckImpression();
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            viewTreeStatusObserver.onActivityPaused(activity);
            // 退到后台时停留时长重新起算；tracked 保留，元素没离开过可视区回到前台就不该重发
            for (ViewImpression impression : viewImpressions) {
                if (impression.getVisibleSince() != 0) {
                    impression.setVisibleSince(0);
                    impression.invalidateRecheck();
                }
            }
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED) {
            activityScope.remove(activity);
        }
    }

    @Override
    public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
        scheduleCheckImpression();
    }

    /**
     * 首尾双触发的节流：间隔到了就立刻检测，没到则把这一轮内的变化合并为一次尾随检测。
     * 界面静止后 ViewTreeObserver 不再回调，尾随检测保证滚动停下瞬间入屏的元素不会漏判。
     */
    private void scheduleCheckImpression() {
        if (checkInterval <= 0) {
            checkImpression();
            return;
        }

        long elapsed = SystemClock.uptimeMillis() - lastCheckTime;
        if (elapsed >= checkInterval) {
            checkImpression();
            return;
        }
        if (trailingCheckScheduled) {
            return;
        }
        trailingCheckScheduled = true;
        TrackMainThread.trackMain().postOnUiThreadDelayed(checkImpressionRunnable, checkInterval - elapsed);
    }

    private void checkImpression() {
        lastCheckTime = SystemClock.uptimeMillis();

        List<ViewImpression> viewImpressions = resumedImpressions();
        if (viewImpressions == null || viewImpressions.isEmpty()) {
            Logger.w(TAG, "ResumedActivity is NULL or This activity has nothing impression");
            return;
        }

        // 回调里允许继续调标记/移除，遍历副本避免并发修改
        for (ViewImpression impression : new ArrayList<>(viewImpressions)) {
            checkViewImpression(impression);
        }
    }

    private void checkViewImpression(ViewImpression impression) {
        View trackedView = impression.getTrackedView();
        if (trackedView == null) {
            return;
        }

        if (!isVisibility(trackedView, impression.getConfig().getImpressionScale())) {
            if (impression.getVisibleSince() != 0) {
                impression.setVisibleSince(0);
                impression.invalidateRecheck();
            }
            impression.setTracked(false);
            return;
        }

        if (impression.isTracked()) {
            return;
        }

        long stayDuration = impression.getConfig().getStayDuration();
        // 与 postOnUiThreadDelayed 用同一个时钟，休眠期间两者不会各走各的
        long now = SystemClock.uptimeMillis();
        if (impression.getVisibleSince() == 0) {
            impression.setVisibleSince(now);
            if (stayDuration > 0) {
                scheduleRecheck(impression, stayDuration);
            }
        }

        if (now - impression.getVisibleSince() >= stayDuration) {
            sendViewImpressionEvent(impression);
        }
    }

    /**
     * 界面静止时 ViewTreeObserver 不再回调，停留时长只能靠这一次定时复检收口。
     */
    private void scheduleRecheck(final ViewImpression impression, long stayDuration) {
        final int token = impression.invalidateRecheck();
        TrackMainThread.trackMain().postOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (impression.getRecheckToken() != token) {
                    return;
                }
                List<ViewImpression> viewImpressions = resumedImpressions();
                if (viewImpressions == null || !viewImpressions.contains(impression)) {
                    return;
                }
                checkViewImpression(impression);
            }
        }, stayDuration + RECHECK_SLACK);
    }

    @Nullable
    private List<ViewImpression> resumedImpressions() {
        if (activityStateProvider == null) return null;
        Activity activity = activityStateProvider.getResumedActivity();
        if (activity == null) return null;
        return activityScope.get(activity);
    }

    private boolean isVisibility(View view, float impressionScale) {
        if (!ViewAttributeUtil.viewVisibilityInParents(view)) {
            return false;
        }

        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            return false;
        }

        // getLocalVisibleRect 已按父容器逐级裁剪并与 window 求交，
        // 滚出滚动容器但屏幕坐标仍在屏内的元素会被判定为不可见
        Rect rect = new Rect();
        if (!view.getLocalVisibleRect(rect)) {
            return false;
        }
        if (impressionScale <= 0) {
            return true;
        }

        float visibleArea = (float) rect.width() * rect.height();
        float totalArea = (float) width * height;
        return visibleArea >= totalArea * impressionScale - SCALE_TOLERANCE;
    }

    private void sendViewImpressionEvent(ViewImpression impression) {
        View trackedView = impression.getTrackedView();
        if (trackedView == null) {
            return;
        }
        impression.setTracked(true);

        ImpressionConfig config = impression.getConfig();
        // 不可重复曝光以 identifier 为准记在全局：视图复用后视图相同而元素不同，
        // 同一元素滚回来又可能落在另一个视图实例上，挂在视图上判不准
        if (!config.isRepeatable() && trackedIdentifiers.contains(impression.getIdentifier())) {
            return;
        }

        if (!shouldTrackImpression(trackedView, impression)) {
            Logger.d(TAG, "impression event is rejected by listener: ", impression.getImpressionEventName());
            return;
        }

        Page<?> page = PageProvider.get().findPage(trackedView);
        if (page == null) {
            Logger.w(TAG, "sendViewImpressionEvent trackedView Activity is NULL");
            return;
        }

        if (!config.isRepeatable()) {
            rememberTrackedIdentifier(impression.getIdentifier());
        }

        Logger.d(TAG, "find View from invisible to visible, send impression event");
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageLevelCustomEvent.Builder()
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setEventName(impression.getImpressionEventName())
                        .setAttributes(mergeAttributes(trackedView, impression))
        );
        notifyImpressionTracked(trackedView, impression);
    }

    private boolean shouldTrackImpression(View view, ViewImpression impression) {
        for (ImpressionListener listener : impressionListeners) {
            if (!listener.shouldTrackImpression(view, impression.getImpressionEventName(), impression.getIdentifier())) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> mergeAttributes(View view, ViewImpression impression) {
        Map<String, String> merged = null;
        for (ImpressionListener listener : impressionListeners) {
            Map<String, String> dynamic = listener.dynamicImpressionAttributes(
                    view, impression.getImpressionEventName(), impression.getIdentifier());
            if (dynamic == null || dynamic.isEmpty()) {
                continue;
            }
            if (merged == null) {
                merged = impression.getEventAttributes() == null
                        ? new HashMap<String, String>()
                        : new HashMap<>(impression.getEventAttributes());
            }
            merged.putAll(dynamic);
        }
        return merged == null ? impression.getEventAttributes() : merged;
    }

    private void notifyImpressionTracked(View view, ViewImpression impression) {
        for (ImpressionListener listener : impressionListeners) {
            listener.onImpressionTracked(view, impression.getImpressionEventName(), impression.getIdentifier());
        }
    }

    private void rememberTrackedIdentifier(String identifier) {
        trackedIdentifiers.add(identifier);
        if (trackedIdentifiers.size() <= MAX_TRACKED_IDENTIFIERS) {
            return;
        }
        if (!trackedIdentifiersOverflowWarned) {
            trackedIdentifiersOverflowWarned = true;
            Logger.w(TAG, "the number of non-repeatable impression identifiers exceeds " + MAX_TRACKED_IDENTIFIERS
                    + ", the earliest record will be evicted, please call resetAllImpressionState() in time");
        }
        Iterator<String> iterator = trackedIdentifiers.iterator();
        iterator.next();
        iterator.remove();
    }

    void addImpressionListener(ImpressionListener listener) {
        if (listener == null || impressionListeners.contains(listener)) {
            return;
        }
        impressionListeners.add(listener);
    }

    void removeImpressionListener(ImpressionListener listener) {
        if (listener == null) {
            return;
        }
        impressionListeners.remove(listener);
    }

    /**
     * 曝光检测的节流间隔，单位毫秒，默认 500。置 0 表示每次视图状态变化都检测。
     * 内部预留，待后续迭代再公开。
     */
    void setCheckInterval(long checkInterval) {
        this.checkInterval = Math.max(checkInterval, 0L);
    }

    public void trackViewImpression(View view, String impressionEventName, Map<String, String> attributes) {
        trackViewImpression(view, impressionEventName, attributes, null, null);
    }

    /**
     * 标记一个视图，元素进入可视区域并满足曝光条件时发送自定义事件。
     * <p>内部预留，待后续迭代再公开。
     *
     * @param identifier 业务上能唯一标识这个元素的值（商品 ID、内容 ID 等），不是视图的标识。
     *                   它决定了"只曝光一次"的判定口径，也是多槽位和精确移除的 key，可为 null
     * @param config     该元素的曝光条件，为 null 时使用全局配置
     */
    void trackViewImpression(View view, String impressionEventName, Map<String, String> attributes,
                             @Nullable String identifier, @Nullable ImpressionConfig config) {
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            return;
        }
        if (!impressionEnabled) {
            Logger.d(TAG, "impression collection is disabled");
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

        ImpressionConfig effectiveConfig = effectiveConfig(config);
        if (!effectiveConfig.isRepeatable() && TextUtils.isEmpty(identifier)) {
            effectiveConfig.setRepeatable(true);
            Logger.w(TAG, "event " + impressionEventName + " is configured as non-repeatable but has no identifier, "
                    + "it has been downgraded to repeatable");
        }
        String slot = TextUtils.isEmpty(identifier) ? ViewImpression.DEFAULT_SLOT : identifier;

        List<ViewImpression> viewImpressions = activityScope.get(activity);
        if (viewImpressions == null) {
            viewImpressions = new ArrayList<>();
            activityScope.put(activity, viewImpressions);
        }

        ViewImpression current = null;
        Iterator<ViewImpression> iterator = viewImpressions.iterator();
        while (iterator.hasNext()) {
            ViewImpression exist = iterator.next();
            View existView = exist.getTrackedView();
            if (existView == null) {
                iterator.remove();
                continue;
            }
            if (existView != view) {
                continue;
            }
            if (exist.getSlot().equals(slot)) {
                current = exist;
            } else if (exist.getImpressionEventName().equals(impressionEventName)) {
                // 复用场景：同一视图先后承载不同元素，事件名不变而 identifier 变了。
                // 旧槽位若留着，视图下次进入可视区时会带着上一个元素的属性再发一次
                exist.invalidateRecheck();
                iterator.remove();
            }
        }

        if (current != null) {
            // 列表刷新会对可见元素原样重标一次，内容没变就保留原有曝光状态，否则下一个检测周期必然多发一次
            if (current.matches(impressionEventName, attributes, effectiveConfig)) {
                viewTreeStatusObserver.onActivityResumed(activity);
                scheduleCheckImpression();
                return;
            }
            current.invalidateRecheck();
            viewImpressions.remove(current);
        }

        Logger.d(TAG, "add view to impression list");
        viewImpressions.add(new ViewImpression(view, slot, impressionEventName, attributes, identifier, effectiveConfig));
        viewTreeStatusObserver.onActivityResumed(activity);
        scheduleCheckImpression();
    }

    /**
     * 只替换属性，不影响曝光状态。只想改属性时用它，重新标记会重置曝光状态。
     * <p>内部预留，待后续迭代再公开。
     */
    void updateViewImpressionAttributes(View view, Map<String, String> attributes, @Nullable String identifier) {
        if (view == null) {
            return;
        }
        ViewImpression impression = findViewImpression(view, identifier);
        if (impression == null) {
            Logger.w(TAG, "the impression slot has not been marked, attributes update is ignored");
            return;
        }
        impression.setEventAttributes(attributes);
    }

    public boolean hasTrackViewImpression(View trackedView) {
        return hasTrackViewImpression(trackedView, null);
    }

    boolean hasTrackViewImpression(View trackedView, @Nullable String identifier) {
        return findViewImpression(trackedView, identifier) != null;
    }

    @Nullable
    private ViewImpression findViewImpression(View view, @Nullable String identifier) {
        if (view == null) {
            return null;
        }
        Activity activity = findViewActivity(view);
        if (activity == null) {
            return null;
        }
        List<ViewImpression> viewImpressions = activityScope.get(activity);
        if (viewImpressions == null || viewImpressions.isEmpty()) {
            return null;
        }
        String slot = TextUtils.isEmpty(identifier) ? ViewImpression.DEFAULT_SLOT : identifier;
        for (ViewImpression impression : viewImpressions) {
            if (impression.getTrackedView() == view && impression.getSlot().equals(slot)) {
                return impression;
            }
        }
        return null;
    }

    /**
     * 移除该视图上的全部标记。
     */
    public void stopTrackViewImpression(View trackedView) {
        stopTrackViewImpression(trackedView, null, true);
    }

    /**
     * 只移除一个标记，该视图上的其他槽位不受影响。
     * <p>内部预留，待后续迭代再公开。
     */
    void stopTrackViewImpression(View trackedView, @Nullable String identifier) {
        stopTrackViewImpression(trackedView, identifier, false);
    }

    private void stopTrackViewImpression(View trackedView, @Nullable String identifier, boolean allSlots) {
        if (trackedView == null) {
            return;
        }
        Activity activity = findViewActivity(trackedView);
        if (activity == null) {
            Logger.e(TAG, "TrackedView context activity is NULL");
            return;
        }

        List<ViewImpression> viewImpressions = activityScope.get(activity);
        if (viewImpressions == null || viewImpressions.isEmpty()) {
            Logger.w(TAG, "ViewImpressions is NULL");
            return;
        }

        String slot = TextUtils.isEmpty(identifier) ? ViewImpression.DEFAULT_SLOT : identifier;
        Iterator<ViewImpression> iterator = viewImpressions.iterator();
        while (iterator.hasNext()) {
            ViewImpression impression = iterator.next();
            if (impression.getTrackedView() != trackedView) {
                continue;
            }
            if (!allSlots && !impression.getSlot().equals(slot)) {
                continue;
            }
            impression.invalidateRecheck();
            iterator.remove();
            Logger.d(TAG, "remove view from impression list");
            if (!allSlots) {
                break;
            }
        }

        if (viewImpressions.isEmpty()) {
            viewTreeStatusObserver.onActivityPaused(activity);
            activityScope.remove(activity);
        }
    }

    /**
     * 清除一个元素的已曝光记录，连同该元素当前的曝光状态一起清掉，
     * 仍停在可视区内的元素无需移出再移入，下一个检测周期就会再曝光一次。
     * <p>内部预留，待后续迭代再公开。
     */
    void resetImpressionState(String identifier) {
        if (TextUtils.isEmpty(identifier)) {
            return;
        }
        trackedIdentifiers.remove(identifier);
        resetTrackedFlag(identifier);
    }

    void resetAllImpressionState() {
        trackedIdentifiers.clear();
        trackedIdentifiersOverflowWarned = false;
        resetTrackedFlag(null);
    }

    private void resetTrackedFlag(@Nullable String identifier) {
        for (List<ViewImpression> viewImpressions : activityScope.values()) {
            for (ViewImpression impression : viewImpressions) {
                if (identifier == null || identifier.equals(impression.getIdentifier())) {
                    impression.setTracked(false);
                }
            }
        }
        scheduleCheckImpression();
    }

    private ImpressionConfig effectiveConfig(@Nullable ImpressionConfig config) {
        return config == null ? globalImpressionConfig.copy() : config.copy();
    }

    @Nullable
    private Activity findViewActivity(View view) {
        Activity activity = ActivityUtil.findActivity(view);
        if (activity == null) {
            Logger.w(TAG, "View context activity is NULL");
            activity = activityStateProvider == null ? null : activityStateProvider.getResumedActivity();
        }
        return activity;
    }
}
