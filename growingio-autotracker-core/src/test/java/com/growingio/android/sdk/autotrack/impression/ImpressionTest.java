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

import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.TrackMainThreadShadow;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Config(manifest = Config.NONE, shadows = {TrackMainThreadShadow.class})
@RunWith(RobolectricTestRunner.class)
public class ImpressionTest {

    private final Application application = ApplicationProvider.getApplicationContext();
    private final List<BaseEvent> events = new ArrayList<>();

    private TrackerContext context;
    private ImpressionProvider impressionProvider;
    private RobolectricActivity activity;

    @Before
    public void setup() {
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, new AutotrackConfig());
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(
                new CoreConfiguration("ImpressionTest", "growingio://impression"), map);

        Autotracker tracker = new Autotracker(application);
        context = tracker.getContext();
        impressionProvider = context.getProvider(ImpressionProvider.class);
        // 关掉节流，每次视图状态变化都立刻检测，用例才能逐步推进
        impressionProvider.setCheckInterval(0);
        activity = Robolectric.buildActivity(RobolectricActivity.class).setup().get();
        makeWindowVisible(activity);

        events.clear();
        TrackMainThreadShadow.callback = events::add;
    }

    @After
    public void tearDown() {
        TrackMainThreadShadow.callback = null;
    }

    /**
     * Robolectric 不跑 ViewRootImpl 的 traversal，AttachInfo 里的窗口可见性一直是 GONE，
     * SDK 判可见的第一步就会挂掉，这里补上。
     */
    private static void makeWindowVisible(RobolectricActivity activity) {
        try {
            View decorView = activity.getWindow().getDecorView();
            Field attachInfoField = View.class.getDeclaredField("mAttachInfo");
            attachInfoField.setAccessible(true);
            Object attachInfo = attachInfoField.get(decorView);
            Field windowVisibilityField = attachInfo.getClass().getDeclaredField("mWindowVisibility");
            windowVisibilityField.setAccessible(true);
            windowVisibilityField.setInt(attachInfo, View.VISIBLE);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("failed to mark the test window visible", e);
        }
    }

    private void checkImpression() {
        impressionProvider.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
    }

    private List<CustomEvent> eventsNamed(String eventName) {
        List<CustomEvent> result = new ArrayList<>();
        for (BaseEvent event : events) {
            if (event instanceof CustomEvent && eventName.equals(((CustomEvent) event).getEventName())) {
                result.add((CustomEvent) event);
            }
        }
        return result;
    }

    private static Map<String, String> attributes(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @Test
    public void markAndUnmarkTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "cpacm", attributes("username", "cpacm"));
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view)).isTrue();

        checkImpression();
        Truth.assertThat(eventsNamed("cpacm")).hasSize(1);
        Truth.assertThat(eventsNamed("cpacm").get(0).getAttributes()).containsEntry("username", "cpacm");

        impressionProvider.stopTrackViewImpression(view);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view)).isFalse();
    }

    @Test
    public void impressionOnlyOnceUntilLeaveTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "stay", null);
        checkImpression();
        checkImpression();
        checkImpression();
        Truth.assertThat(eventsNamed("stay")).hasSize(1);

        // 离开可视区再进入才会再发
        view.setVisibility(View.GONE);
        checkImpression();
        view.setVisibility(View.VISIBLE);
        checkImpression();
        Truth.assertThat(eventsNamed("stay")).hasSize(2);
    }

    @Test
    public void remarkWithSameContentKeepsStateTest() {
        View view = activity.getTextView();
        Map<String, String> attrs = attributes("goods", "1");
        impressionProvider.trackViewImpression(view, "goods", attrs, "g1", null);
        checkImpression();
        Truth.assertThat(eventsNamed("goods")).hasSize(1);

        // 列表刷新原样重标：事件名、属性、配置都没变，保留曝光状态
        impressionProvider.trackViewImpression(view, "goods", attributes("goods", "1"), "g1", null);
        checkImpression();
        Truth.assertThat(eventsNamed("goods")).hasSize(1);

        // 属性变了视为一次新的标记，曝光状态重置
        impressionProvider.trackViewImpression(view, "goods", attributes("goods", "2"), "g1", null);
        checkImpression();
        Truth.assertThat(eventsNamed("goods")).hasSize(2);
    }

    @Test
    public void reuseDropsStaleSlotTest() {
        View view = activity.getTextView();
        view.setVisibility(View.GONE);
        impressionProvider.trackViewImpression(view, "goods", attributes("goods", "1"), "g1", null);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view, "g1")).isTrue();

        // 同一事件名在一个视图上只保留一个槽位，identifier 变了说明承载的元素换了
        impressionProvider.trackViewImpression(view, "goods", attributes("goods", "2"), "g2", null);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view, "g1")).isFalse();
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view, "g2")).isTrue();

        view.setVisibility(View.VISIBLE);
        checkImpression();
        List<CustomEvent> goodsEvents = eventsNamed("goods");
        Truth.assertThat(goodsEvents).hasSize(1);
        Truth.assertThat(goodsEvents.get(0).getAttributes()).containsEntry("goods", "2");
    }

    @Test
    public void multiSlotOnOneViewTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "card", null, "card", null);
        impressionProvider.trackViewImpression(view, "badge", null, "badge", null);
        checkImpression();
        Truth.assertThat(eventsNamed("card")).hasSize(1);
        Truth.assertThat(eventsNamed("badge")).hasSize(1);

        impressionProvider.stopTrackViewImpression(view, "badge");
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view, "badge")).isFalse();
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view, "card")).isTrue();

        impressionProvider.stopTrackViewImpression(view);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view, "card")).isFalse();
    }

    @Test
    public void updateAttributesDoesNotRetrackTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "price", attributes("price", "10"), "p1", null);
        checkImpression();
        Truth.assertThat(eventsNamed("price")).hasSize(1);

        impressionProvider.updateViewImpressionAttributes(view, attributes("price", "20"), "p1");
        checkImpression();
        Truth.assertThat(eventsNamed("price")).hasSize(1);

        // 更新后保留的是更新后的属性
        view.setVisibility(View.GONE);
        checkImpression();
        view.setVisibility(View.VISIBLE);
        checkImpression();
        List<CustomEvent> priceEvents = eventsNamed("price");
        Truth.assertThat(priceEvents).hasSize(2);
        Truth.assertThat(priceEvents.get(1).getAttributes()).containsEntry("price", "20");
    }

    @Test
    public void nonRepeatableTest() {
        View view = activity.getTextView();
        ImpressionConfig config = ImpressionConfig.create(0f, 0L, false);
        impressionProvider.trackViewImpression(view, "once", null, "u1", config);
        checkImpression();
        Truth.assertThat(eventsNamed("once")).hasSize(1);

        view.setVisibility(View.GONE);
        checkImpression();
        view.setVisibility(View.VISIBLE);
        checkImpression();
        Truth.assertThat(eventsNamed("once")).hasSize(1);

        // 重置后无需移出可视区即可再次曝光
        impressionProvider.resetImpressionState("u1");
        checkImpression();
        Truth.assertThat(eventsNamed("once")).hasSize(2);

        impressionProvider.resetAllImpressionState();
        checkImpression();
        Truth.assertThat(eventsNamed("once")).hasSize(3);
    }

    @Test
    public void nonRepeatableWithoutIdentifierIsDowngradedTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "fallback", null, null, ImpressionConfig.create(0f, 0L, false));
        checkImpression();
        view.setVisibility(View.GONE);
        checkImpression();
        view.setVisibility(View.VISIBLE);
        checkImpression();
        // 缺少 identifier 时降级为可重复曝光
        Truth.assertThat(eventsNamed("fallback")).hasSize(2);
    }

    @Test
    public void stayDurationTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "stayed", null, "s1", ImpressionConfig.create(0f, 1000L, true));
        checkImpression();
        Truth.assertThat(eventsNamed("stayed")).isEmpty();

        // 界面静止不产生回调，靠一次性定时复检收口
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100));
        Truth.assertThat(eventsNamed("stayed")).hasSize(1);
    }

    @Test
    public void stayDurationRestartsAfterLeaveTest() {
        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "slide", null, "s1", ImpressionConfig.create(0f, 1000L, true));
        checkImpression();

        // 滑过：不满一秒就离开，不算曝光
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500));
        view.setVisibility(View.GONE);
        checkImpression();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(600));
        Truth.assertThat(eventsNamed("slide")).isEmpty();

        view.setVisibility(View.VISIBLE);
        checkImpression();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100));
        Truth.assertThat(eventsNamed("slide")).hasSize(1);
    }

    /**
     * 高 40 的子视图放进高 20 的裁剪容器，可见面积占比 0.5。
     */
    /**
     * 往内容区挂一条 container -> child 的可见视图链，两级都铺满 100x40，
     * 用来构造"父容器设了忽略策略，子视图被连带忽略"的场景。
     *
     * @return 下标 0 是 container，1 是 child
     */
    private static View[] addVisibleChain(RobolectricActivity activity) {
        FrameLayout container = new FrameLayout(activity);
        View child = new View(activity);
        container.addView(child, new ViewGroup.LayoutParams(100, 40));
        ((ViewGroup) activity.findViewById(android.R.id.content))
                .addView(container, new ViewGroup.LayoutParams(100, 40));
        container.measure(View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY));
        container.layout(0, 0, 100, 40);
        return new View[]{container, child};
    }

    private static View addHalfClippedChild(RobolectricActivity activity) {
        FrameLayout clipper = new FrameLayout(activity);
        View child = new View(activity);
        clipper.addView(child, new ViewGroup.LayoutParams(100, 40));
        ((ViewGroup) activity.findViewById(android.R.id.content)).addView(clipper, new ViewGroup.LayoutParams(100, 20));
        clipper.measure(View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(20, View.MeasureSpec.EXACTLY));
        clipper.layout(0, 0, 100, 20);
        return child;
    }

    @Test
    public void impressionScaleTest() {
        View child = addHalfClippedChild(activity);

        impressionProvider.trackViewImpression(child, "half", null, "half", ImpressionConfig.create(0.5f, 0L, true));
        impressionProvider.trackViewImpression(child, "full", null, "full", ImpressionConfig.create(1.0f, 0L, true));
        checkImpression();

        Truth.assertThat(eventsNamed("half")).hasSize(1);
        Truth.assertThat(eventsNamed("full")).isEmpty();
    }

    @Test
    public void listenerTest() {
        final List<String> tracked = new ArrayList<>();
        final boolean[] veto = {true};
        ImpressionListener listener = new SimpleImpressionListener() {
            @Override
            public boolean shouldTrackImpression(View view, String eventName, String identifier) {
                return !veto[0];
            }

            @Override
            public Map<String, String> dynamicImpressionAttributes(View view, String eventName, String identifier) {
                return attributes("position", "3");
            }

            @Override
            public void onImpressionTracked(View view, String eventName, String identifier) {
                tracked.add(eventName + "@" + identifier);
            }
        };
        impressionProvider.addImpressionListener(listener);

        View view = activity.getTextView();
        impressionProvider.trackViewImpression(view, "cb", attributes("static", "1"), "c1", null);
        checkImpression();
        Truth.assertThat(eventsNamed("cb")).isEmpty();
        Truth.assertThat(tracked).isEmpty();

        // 离开可视区再进入时重新询问
        veto[0] = false;
        view.setVisibility(View.GONE);
        checkImpression();
        view.setVisibility(View.VISIBLE);
        checkImpression();

        Truth.assertThat(eventsNamed("cb")).hasSize(1);
        Truth.assertThat(eventsNamed("cb").get(0).getAttributes()).containsEntry("static", "1");
        Truth.assertThat(eventsNamed("cb").get(0).getAttributes()).containsEntry("position", "3");
        Truth.assertThat(tracked).isEqualTo(Arrays.asList("cb@c1"));

        impressionProvider.removeImpressionListener(listener);
        view.setVisibility(View.GONE);
        checkImpression();
        view.setVisibility(View.VISIBLE);
        checkImpression();
        Truth.assertThat(eventsNamed("cb")).hasSize(2);
        Truth.assertThat(eventsNamed("cb").get(1).getAttributes()).doesNotContainKey("position");
        Truth.assertThat(tracked).hasSize(1);
    }

    @Test
    public void impressionConfigTest() {
        ImpressionConfig config = new ImpressionConfig().setImpressionScale(2f).setStayDuration(-1L);
        Truth.assertThat(config.getImpressionScale()).isEqualTo(1f);
        Truth.assertThat(config.getStayDuration()).isEqualTo(0L);
        Truth.assertThat(config.isRepeatable()).isTrue();

        Truth.assertThat(config.copy()).isEqualTo(config);
        Truth.assertThat(config.copy().hashCode()).isEqualTo(config.hashCode());
        Truth.assertThat(config.copy().setRepeatable(false)).isNotEqualTo(config);

        AutotrackConfig autotrackConfig = new AutotrackConfig().setImpressionScale(0.5f);
        Truth.assertThat(autotrackConfig.getImpressionConfig().getImpressionScale()).isEqualTo(0.5f);
        Truth.assertThat(autotrackConfig.setImpressionScale(-1f).getImpressionScale()).isEqualTo(0f);
    }

    @Test
    public void globalConfigTest() {
        // 新的 tracker 要先于它所观察的 activity 建立，否则拿不到 resumedActivity
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, new AutotrackConfig()
                .setImpressionConfig(ImpressionConfig.create(1.0f, 0L, true)));
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(
                new CoreConfiguration("ImpressionTest", "growingio://impression"), map);
        Autotracker tracker = new Autotracker(application);
        ImpressionProvider provider = tracker.getContext().getProvider(ImpressionProvider.class);
        provider.setCheckInterval(0);

        RobolectricActivity globalActivity = Robolectric.buildActivity(RobolectricActivity.class).setup().get();
        makeWindowVisible(globalActivity);
        View child = addHalfClippedChild(globalActivity);

        // 全局默认 scale = 1，半遮挡的元素不曝光
        provider.trackViewImpression(child, "global", null, null, null);
        provider.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
        Truth.assertThat(eventsNamed("global")).isEmpty();

        // 单元素 config 优先于全局配置
        provider.trackViewImpression(child, "element", null, null, ImpressionConfig.create(0.5f, 0L, true));
        provider.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
        Truth.assertThat(eventsNamed("element")).hasSize(1);
    }

    @Test
    public void impressionDisabledTest() {
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, new AutotrackConfig().setImpressionEnabled(false));
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(
                new CoreConfiguration("ImpressionTest", "growingio://impression"), map);
        Autotracker tracker = new Autotracker(application);
        ImpressionProvider provider = tracker.getContext().getProvider(ImpressionProvider.class);
        provider.setCheckInterval(0);

        RobolectricActivity disabledActivity = Robolectric.buildActivity(RobolectricActivity.class).setup().get();
        makeWindowVisible(disabledActivity);

        provider.trackViewImpression(disabledActivity.getTextView(), "disabled", null);
        Truth.assertThat(provider.hasTrackViewImpression(disabledActivity.getTextView())).isFalse();
        provider.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
        Truth.assertThat(eventsNamed("disabled")).isEmpty();
    }

    @Test
    public void autotrackDisabledTest() {
        // 曝光受无埋点开关约束：setAutotrack(false) 时 setImpressionEnabled(true) 也不采集
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, new AutotrackConfig()
                .setImpressionEnabled(true)
                .setAutotrack(false));
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(
                new CoreConfiguration("ImpressionTest", "growingio://impression"), map);
        Autotracker tracker = new Autotracker(application);
        ImpressionProvider provider = tracker.getContext().getProvider(ImpressionProvider.class);
        provider.setCheckInterval(0);

        RobolectricActivity disabledActivity = Robolectric.buildActivity(RobolectricActivity.class).setup().get();
        makeWindowVisible(disabledActivity);

        provider.trackViewImpression(disabledActivity.getTextView(), "autotrackOff", null);
        Truth.assertThat(provider.hasTrackViewImpression(disabledActivity.getTextView())).isFalse();
        provider.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
        Truth.assertThat(eventsNamed("autotrackOff")).isEmpty();
    }

    // ---------- 无埋点忽略规则 ----------

    @Test
    public void ignoreSelfSuppressesMarkTest() {
        View view = activity.getTextView();
        ViewAttributeUtil.setIgnorePolicy(view, IgnorePolicy.IGNORE_SELF);

        impressionProvider.trackViewImpression(view, "ignore_self", null);
        // 标记阶段就被拒绝，连槽位都不建
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view)).isFalse();
        checkImpression();
        Truth.assertThat(eventsNamed("ignore_self")).isEmpty();
    }

    @Test
    public void ignoreAllOnSelfSuppressesMarkTest() {
        View view = activity.getTextView();
        ViewAttributeUtil.setIgnorePolicy(view, IgnorePolicy.IGNORE_ALL);

        impressionProvider.trackViewImpression(view, "ignore_all_self", null);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(view)).isFalse();
        checkImpression();
        Truth.assertThat(eventsNamed("ignore_all_self")).isEmpty();
    }

    @Test
    public void ignoreChildOnParentSuppressesMarkTest() {
        View[] chain = addVisibleChain(activity);
        ViewAttributeUtil.setIgnorePolicy(chain[0], IgnorePolicy.IGNORE_CHILD);

        impressionProvider.trackViewImpression(chain[1], "ignore_child", null);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(chain[1])).isFalse();
        checkImpression();
        Truth.assertThat(eventsNamed("ignore_child")).isEmpty();
    }

    @Test
    public void ignoreChildOnParentDoesNotSuppressItselfTest() {
        View[] chain = addVisibleChain(activity);
        ViewAttributeUtil.setIgnorePolicy(chain[0], IgnorePolicy.IGNORE_CHILD);

        impressionProvider.trackViewImpression(chain[0], "ignore_child_self", null);
        checkImpression();
        Truth.assertThat(eventsNamed("ignore_child_self")).hasSize(1);
    }

    @Test
    public void ignoreAllOnAncestorSuppressesMarkTest() {
        // 忽略策略沿父链逐级上溯，不止看直接父容器
        View[] chain = addVisibleChain(activity);
        ViewAttributeUtil.setIgnorePolicy((View) chain[0].getParent(), IgnorePolicy.IGNORE_ALL);

        impressionProvider.trackViewImpression(chain[1], "ignore_ancestor", null);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(chain[1])).isFalse();
        checkImpression();
        Truth.assertThat(eventsNamed("ignore_ancestor")).isEmpty();
    }

    @Test
    public void ignoreSelfOnParentDoesNotSuppressChildTest() {
        // IGNORE_SELF 只作用于设置它的那个视图，不影响子视图
        View[] chain = addVisibleChain(activity);
        ViewAttributeUtil.setIgnorePolicy(chain[0], IgnorePolicy.IGNORE_SELF);

        impressionProvider.trackViewImpression(chain[1], "parent_ignore_self", null);
        checkImpression();
        Truth.assertThat(eventsNamed("parent_ignore_self")).hasSize(1);
    }

    @Test
    public void ownPolicyShortCircuitsAncestorLookupTest() {
        // ViewAttributeUtil.isIgnoredView 的既有行为：视图自身设了策略就不再上溯父链，
        // 因此子视图的 IGNORE_CHILD 会屏蔽掉父容器的 IGNORE_ALL。这里把现状钉住
        View[] chain = addVisibleChain(activity);
        ViewAttributeUtil.setIgnorePolicy(chain[0], IgnorePolicy.IGNORE_ALL);
        ViewAttributeUtil.setIgnorePolicy(chain[1], IgnorePolicy.IGNORE_CHILD);

        impressionProvider.trackViewImpression(chain[1], "short_circuit", null);
        checkImpression();
        Truth.assertThat(eventsNamed("short_circuit")).hasSize(1);
    }

    @Test
    public void noIgnorePolicyStillMarksTest() {
        View[] chain = addVisibleChain(activity);

        impressionProvider.trackViewImpression(chain[1], "no_ignore", null);
        Truth.assertThat(impressionProvider.hasTrackViewImpression(chain[1])).isTrue();
        checkImpression();
        Truth.assertThat(eventsNamed("no_ignore")).hasSize(1);
    }
}
