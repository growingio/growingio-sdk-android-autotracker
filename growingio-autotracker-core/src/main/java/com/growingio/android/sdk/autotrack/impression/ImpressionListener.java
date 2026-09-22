/*
 * Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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

import android.view.View;

import androidx.annotation.Nullable;

import java.util.Map;

/**
 * 曝光回调。三个方法都在主线程同步执行，处在曝光检测的链路上，实现中不要做耗时操作。
 * <p>只关心其中一两个方法时，继承 {@link SimpleImpressionListener}。
 */
public interface ImpressionListener {

    /**
     * 返回 false 则本次不发送。元素离开可视区再次进入时会重新询问；
     * 注册了多个 listener 时任一返回 false 即不发送。
     */
    boolean shouldTrackImpression(View view, String eventName, @Nullable String identifier);

    /**
     * 补充曝光时刻才能确定的属性（当时的排序位置、实时价格等），
     * 与标记时的静态属性合并，同名键以动态属性为准。
     */
    @Nullable
    Map<String, String> dynamicImpressionAttributes(View view, String eventName, @Nullable String identifier);

    /**
     * 事件已生成。
     */
    void onImpressionTracked(View view, String eventName, @Nullable String identifier);
}
