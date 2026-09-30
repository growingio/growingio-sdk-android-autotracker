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
 * {@link ImpressionListener} 的空实现，按需覆写其中的方法。
 * <p>内部预留，待后续迭代再公开。
 */
abstract class SimpleImpressionListener implements ImpressionListener {

    @Override
    public boolean shouldTrackImpression(View view, String eventName, @Nullable String identifier) {
        return true;
    }

    @Nullable
    @Override
    public Map<String, String> dynamicImpressionAttributes(View view, String eventName, @Nullable String identifier) {
        return null;
    }

    @Override
    public void onImpressionTracked(View view, String eventName, @Nullable String identifier) {
    }
}
