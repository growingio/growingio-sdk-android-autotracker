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

import com.growingio.android.sdk.track.utils.GIOProviders;

/**
 * 所有关于数据与发送逻辑的相关配置参数
 */
public interface SendPolicyProvider {

    /**
     * @return 最多多少条数据入库后需触发一次发送, 默认值300条
     */
    int bulkSize();

    /**
     * @return 某条数据入库后, 最长多长时间要触发一次发送, 默认值30_000毫秒, 单位毫秒
     */
    long flushInterval();

    void setFlushInterval(long flushInterval);

    /**
     * @return 限制一天内移动流量下允许发送的流量数, 默认值3M, 单位bytes
     */
    int cellularDataLimit();

    void setCellularDataLimit(int cellularDataLimit);

    /**
     * @return 数据处理结束后, GIOSenderService将StopSelf, 允许配置延迟
     */
    int delayStopTimeMills();

    /**
     * @return 事件的有效期, 单位毫秒, 默认7天, 超期事件将被删除
     */
    long eventValidPeriodMills();

    /**
     * 默认发送策略
     */
    class SendPolicy implements SendPolicyProvider {
        private long mFlushInterval = 15 * 1000L;
        private int mCellularDataLimit = 10 * 1024 * 1024;

        public static SendPolicyProvider get() {
            return GIOProviders.provider(SendPolicyProvider.class, new GIOProviders.DefaultCallback<SendPolicyProvider>() {
                @Override
                public SendPolicyProvider value() {
                    return new SendPolicy();
                }
            });
        }

        @Override
        public int bulkSize() {
            return 300;
        }

        @Override
        public long flushInterval() {
            return mFlushInterval;
        }

        @Override
        public int cellularDataLimit() {
            return mCellularDataLimit;
        }

        @Override
        public void setFlushInterval(long flushInterval) {
            this.mFlushInterval = flushInterval;
        }

        @Override
        public void setCellularDataLimit(int cellularDataLimit) {
            this.mCellularDataLimit = cellularDataLimit;
        }

        @Override
        public int delayStopTimeMills() {
            return 70_000;
        }

        @Override
        public long eventValidPeriodMills() {
            return 7 * 24 * 60 * 60_000;
        }
    }
}
