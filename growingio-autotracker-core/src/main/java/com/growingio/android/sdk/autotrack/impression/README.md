# 元素曝光采集

为视图标记一个事件，元素进入可视区域并满足曝光条件时，自动发送对应的自定义事件（`cstm`）。

实现在无埋点 SDK 的 `growingio-autotracker-core` 中，无需额外依赖。曝光事件带页面路径，页面体系本身属于无埋点能力，因此纯埋点 SDK（`growingio-tracker-core`）不提供该能力。

曝光采集属于无埋点能力的一部分，受 `setAutotrack` 约束：关掉无埋点后曝光同样不采集。`setImpressionEnabled` 用于在无埋点开启的前提下单独关掉曝光。

## 快速开始

在元素上屏前标记即可，列表场景直接在 `onBindViewHolder` 里标记复用的 item 或其子视图：

```java
@Override
public void onBindViewHolder(GoodsViewHolder holder, int position) {
    Goods goods = goodsList.get(position);
    holder.bind(goods);

    Map<String, String> attributes = new HashMap<>();
    attributes.put("goods_id", goods.getId());
    attributes.put("position", String.valueOf(position));
    GrowingAutotracker.get().trackViewImpression(holder.itemView, "goods_impression", attributes);
}
```

## API

全部挂在 `GrowingAutotracker` 上，内部都会切到主线程执行，在子线程调用是安全的。

| 方法 | 说明 |
|---|---|
| `trackViewImpression(view, eventName)` | 标记，使用全局配置 |
| `trackViewImpression(view, eventName, attributes)` | 同上，附带静态属性 |
| `stopTrackViewImpression(view)` | 移除该视图上的全部标记 |

标记遵循无埋点的忽略规则：被 `ignoreView` 设为 `IGNORE_SELF` / `IGNORE_ALL` 的视图，或父链上有 `IGNORE_ALL` / `IGNORE_CHILD` 的视图，标记会被忽略并输出一条警告日志。

重复标记同一事件名时，事件名、属性、配置三者都没有变化则不重置曝光状态；任一项发生变化视为一次新的标记。因此列表刷新时对可见元素原样重标一次是安全的，ViewHolder 复用后绑定新数据重新标记也不会残留上一行的内容。

## 曝光条件

| 配置项 | 含义 | 默认值 |
|---|---|---|
| `impressionScale` | 可见面积占元素自身面积的比例阈值，有效范围 [0,1] | 0，露出即算 |
| `stayDuration` | 连续可见需要满足的最小时长，单位毫秒 | 0，无需停留 |

两项通过全局配置统一设置，挂在 `AutotrackConfiguration` 上：

```java
configuration.setImpressionEnabled(true)                  // 曝光开关，默认 true；setAutotrack(false) 时无论如何都不采集
        .setImpressionCheckInterval(500)                  // 检测节流间隔，单位毫秒，默认 500
        .setImpressionConfig(ImpressionConfig.create(0.5f, 1000L));
```

配置在 SDK 启动时读取，启动之后再改不会生效。`setImpressionScale` 仍然可用，等价于只设置 `impressionConfig` 里的同名项。

可见性由 `View.getLocalVisibleRect()` 判定，它已按父容器逐级裁剪并与窗口求交。因此滚出了滚动容器、但屏幕坐标仍落在屏内的元素，会被判定为不可见。

## 重复曝光

默认支持重复曝光：元素满足条件发送一次事件后，**离开可视区再次进入**会再发一次。以下情况不会重复发送：

- 元素一直停留在可视区内，无论停留多久
- App 退到后台再回到前台，期间元素没有离开过可视区
- 重复标记，但事件名、属性、配置三者都没有变化

## 实现要点

- **检测由视图树变化驱动**：`ViewTreeStatusObserver` 监听所在 Activity 的布局、滚动与窗口事件，检测按 `impressionCheckInterval` 做首尾双触发的节流——间隔到了立刻检测，间隔内的变化合并为一次尾随检测，滚动停止瞬间入屏的元素不会漏判。
- **停留时长靠一次性定时复检收口**：界面静止后视图树不再产生回调，元素上的令牌保证反复进出可视区不累积待执行任务。
- **标记按 Activity 分组**，视图以弱引用持有，Activity 销毁时整组释放。

## 限制

- **不做遮挡检测。** 被上层视图完全盖住的元素仍按可见处理。
- 被 `ignoreView` 忽略的视图不会曝光。
- 元素或其祖先设置了缩放、旋转时，面积占比的判定不准确。
