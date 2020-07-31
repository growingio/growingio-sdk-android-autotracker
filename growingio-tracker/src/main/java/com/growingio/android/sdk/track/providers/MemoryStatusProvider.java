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

package com.growingio.android.sdk.track.providers;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.Nullable;

import com.growingio.android.sdk.track.utils.GIOProviders;

/**
 * 提供当前App的内存状态与策略
 */
public interface MemoryStatusProvider {
    /**
     * 触发检查当前App内存情况
     */
    void check();

    /**
     * @return 当前内存情况值
     */
    @Nullable
    ActivityManager.MemoryInfo memoryInfo();

    /**
     * @return 返回当前内存情况下， 一条Http请求中最多包含事件条数, 默认50条， 低内存情况下3条
     */
    int numOfMaxEventsPerRequest();

    class MemoryPolicy implements MemoryStatusProvider {

        private final Context mContext;
        private ActivityManager.MemoryInfo mMemoryInfo;
        public MemoryPolicy(Context context) {
            this.mContext = context.getApplicationContext();
        }

        public static MemoryStatusProvider get(final Context context) {
            return GIOProviders.provider(MemoryStatusProvider.class, new GIOProviders.DefaultCallback<MemoryStatusProvider>() {
                @Override
                public MemoryStatusProvider value() {
                    return new MemoryPolicy(context);
                }
            });
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void check() {
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            this.mMemoryInfo = memoryInfo;
        }

        @Override
        @Nullable
        public ActivityManager.MemoryInfo memoryInfo() {
            return mMemoryInfo;
        }

        @Override
        public int numOfMaxEventsPerRequest() {
            ActivityManager.MemoryInfo info = mMemoryInfo;
            if (info != null && info.lowMemory) {
                return 3;
            }
            return 50;
        }
    }
}
