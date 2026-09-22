# 元素曝光采集

为视图标记一个事件，元素进入可视区域并满足曝光条件时，自动发送对应的自定义事件（`cstm`）。

实现在无埋点 SDK 的 `growingio-autotracker-core` 中，无需额外依赖。曝光事件带页面路径，页面体系本身属于无埋点能力，因此纯埋点 SDK（`growingio-tracker-core`）不提供该能力。

曝光采集是主动标记的能力，与无埋点开关（`setAutotrack`）相互独立：关掉无埋点后标记的元素依然会曝光。总开关是 `setImpressionEnabled`。

## 快速开始

在列表绑定数据的地方标记即可，不需要在视图复用时清理。标记 item 里的某个子视图（角标、价格标签等）同样如此。

```java
holder.itemView.setOnBind(goods -> {
    GrowingAutotracker.get().trackViewImpression(
            holder.itemView,
            "goods_impression",
            attributes,              // 静态属性
            goods.getId(),           // identifier
            null);                   // config，null 表示用全局配置
});
```

`identifier` 填**业务上能唯一标识这个元素的值**（商品 ID、内容 ID 等），不是视图的标识。它决定了"只曝光一次"的判定口径，也是多槽位和精确移除的 key。

## API

全部挂在 `GrowingAutotracker` 上，内部都会切到主线程执行，在子线程调用是安全的。

| 方法 | 说明 |
|---|---|
| `trackViewImpression(view, eventName)` | 标记，使用全局配置，写入默认槽位 |
| `trackViewImpression(view, eventName, attributes)` | 同上，附带静态属性 |
| `trackViewImpression(view, eventName, attributes, identifier, config)` | 完整形式，`identifier` 与 `config` 均可传 null |
| `updateViewImpressionAttributes(view, attributes, identifier)` | 只替换属性，不影响曝光状态 |
| `stopTrackViewImpression(view)` | 移除该视图上的全部标记 |
| `stopTrackViewImpression(view, identifier)` | 只移除一个标记 |
| `resetViewImpressionState(identifier)` / `resetAllViewImpressionState()` | 清除"只曝光一次"的记录 |
| `addViewImpressionListener(listener)` / `removeViewImpressionListener(listener)` | 曝光回调 |

一个视图可以挂多个标记，槽位以 `identifier` 区分，各自独立判定、独立发送；`identifier` 传 null 时写入默认槽位。

```java
GrowingAutotracker.get().trackViewImpression(itemView, "card_impression", null, "card", null);
GrowingAutotracker.get().trackViewImpression(itemView, "badge_impression", null, "badge", null);

GrowingAutotracker.get().stopTrackViewImpression(itemView, "badge");  // card 不受影响
```

**同一个事件名在一个视图上只保留一个槽位。** 再次标记时 `identifier` 变了，意味着这个视图承载的元素换了——ViewHolder 及其子视图被复用就是这种情况——旧槽位随即丢弃。所以在 `onBindViewHolder` 里直接标记就行，既不需要在复用时清理，也不会残留上一行的属性。

## 曝光条件

| 配置项 | 含义 | 默认值 |
|---|---|---|
| `impressionScale` | 可见面积占元素自身面积的比例阈值，有效范围 [0,1] | 0，露出即算 |
| `stayDuration` | 连续可见需要满足的最小时长，单位毫秒 | 0，无需停留 |
| `repeatable` | 是否允许同一元素多次曝光 | true |

三项都可以按元素单独配置，也可以配全局默认值。优先级：**单元素 `config` > 全局 `impressionConfig` > 默认值**。

```java
ImpressionConfig config = ImpressionConfig.create(0.5f, 1000L, false);
GrowingAutotracker.get().trackViewImpression(itemView, "goods_impression", null, goodsId, config);
```

全局配置挂在 `AutotrackConfiguration` 上：

```java
configuration.setImpressionEnabled(true)                  // 采集总开关，默认 true
        .setImpressionCheckInterval(500)                  // 检测节流间隔，单位毫秒，默认 500
        .setImpressionConfig(ImpressionConfig.create(0.5f, 1000L, true));
```

这三项在 SDK 启动时读取，启动之后再改不会生效。`setImpressionScale` 仍然可用，等价于只设置 `impressionConfig` 里的同名项。

全局默认值不要配 `repeatable = false`：它依赖 `identifier`，而未指定 `identifier` 的元素会被降级处理（见下文）。

可见性由 `View.getLocalVisibleRect()` 判定，它已按父容器逐级裁剪并与窗口求交。因此滚出了滚动容器、但屏幕坐标仍落在屏内的元素，会被判定为不可见。

## 曝光时机

元素满足曝光条件时发送一次事件，此后**离开可视区再次进入**才会再发。以下情况不会重复发送：

- 元素一直停留在可视区内，无论停留多久
- App 退到后台再回到前台，期间元素没有离开过可视区
- 重复标记，但事件名、属性、配置三者都没有变化

最后一条使得列表刷新时对可见元素原样重标一次是安全的。三者中任一项发生变化则视为一次新的标记，曝光状态重置，元素满足条件时会再发送一次——所以**只想改属性时请用 `updateViewImpressionAttributes`**，它不会触发重新曝光。

更新属性后，元素上保留的是更新后的属性。此后列表刷新若仍按原属性重新标记，将被视为属性发生变化，曝光状态随之重置，元素未离开可视区也会再次曝光。

## 只曝光一次

`repeatable = false` 表示同一元素全程只曝光一次。

**此时必须指定 `identifier`。**"只曝光一次"的对象是元素而不是视图：ViewHolder 复用后视图相同而元素不同，同一元素滚回来又可能落在另一个 ViewHolder 实例上。已曝光记录因此按 `identifier` 记在全局集合里，缺少 `identifier` 就无法区分元素——此时配置会被降级为 `repeatable = true` 并输出告警日志。

该记录不区分事件名：同一 `identifier` 曝光一次后，以其标记的其他事件名均不再发送。需要各自独立判定时，应使用不同的 `identifier`。

下拉刷新、切换账号、切换数据源等场景需要主动清理记录：

```java
GrowingAutotracker.get().resetViewImpressionState(goodsId);   // 清一个
GrowingAutotracker.get().resetAllViewImpressionState();       // 全清
```

重置会连同元素当前的曝光状态一起清掉，因此仍停在可视区内的元素无需移出再移入，下一个检测周期就会再曝光一次。

记录不随 session 自动重置。全局集合上限 10000 条，超限按插入顺序淘汰最早的记录并告警一次。

## 曝光回调

```java
GrowingAutotracker.get().addViewImpressionListener(new SimpleImpressionListener() {
    @Override
    public void onImpressionTracked(View view, String eventName, String identifier) {
        // ...
    }
});
```

| 方法 | 用途 |
|---|---|
| `shouldTrackImpression(view, eventName, identifier)` | 返回 false 则本次不发送。元素离开可视区再次进入时会重新询问；注册了多个 listener 时任一返回 false 即不发送 |
| `dynamicImpressionAttributes(view, eventName, identifier)` | 补充曝光时刻才能确定的属性（当时的排序位置、实时价格等），与标记时的静态属性合并，同名键以动态属性为准 |
| `onImpressionTracked(view, eventName, identifier)` | 事件已生成 |

`ImpressionListener` 的三个方法都要实现，只关心其中一两个时继承 `SimpleImpressionListener`。

回调均在主线程同步执行，处在曝光检测的链路上，实现中不要做耗时操作。listener 以强引用持有，页面销毁时记得移除。

## 实现要点

- **检测由视图树变化驱动**：`ViewTreeStatusObserver` 监听所在 Activity 的布局、滚动与窗口事件，检测按 `impressionCheckInterval` 做首尾双触发的节流——间隔到了立刻检测，间隔内的变化合并为一次尾随检测，滚动停止瞬间入屏的元素不会漏判。
- **停留时长靠一次性定时复检收口**：界面静止后视图树不再产生回调，元素上的令牌保证反复进出可视区不累积待执行任务。
- **标记按 Activity 分组**，视图以弱引用持有，Activity 销毁时整组释放。

## 限制

- **不做遮挡检测。** 被上层视图完全盖住的元素仍按可见处理。
- 被 `ignoreView` 忽略的视图不会曝光。
- 元素或其祖先设置了缩放、旋转时，面积占比的判定不准确。
