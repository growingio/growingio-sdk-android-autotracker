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
 * 数据发送层， 关于使用DBJobScheduler相关配置
 */
public interface DBJobScheduleProvider {
    /**
     * @return true 没有网络时， 使用JobScheduler调度等待网络, 可能会后台唤醒App
     */
    boolean useJobScheduler();

    /**
     * @return 由于Android 8不允许后台startService， 此Id用于模拟startService功能
     */
    int scheduleIdForStartCommand();

    /**
     * @return 使用JobScheduler等待网络的Id, 与useJobScheduler搭配使用
     */
    int scheduleIdForWaitNet();

    /**
     * @return 使用JobScheduler等待网络时, 传递给setMinimumLatency的值
     */
    int waitNetMinimumLatency();

    /**
     * @return 使用JobScheduler等待网络时, 传递给setOverrideDeadline的值
     */
    int waitNetOverrideDeadline();


    /**
     * 默认关于JobScheduler的配置
     */
    class JobSchedulePolicy implements DBJobScheduleProvider {

        public static final int JOB_SCHEDULER_START_COMMAND = 2019121119;
        public static final int JOB_SCHEDULER_WAIT_NET = 2019121120;

        public static DBJobScheduleProvider get() {
            return GIOProviders.provider(DBJobScheduleProvider.class, new GIOProviders.DefaultCallback<DBJobScheduleProvider>() {
                @Override
                public DBJobScheduleProvider value() {
                    return new JobSchedulePolicy();
                }
            });
        }

        @Override
        public boolean useJobScheduler() {
            return true;
        }

        @Override
        public int scheduleIdForStartCommand() {
            return JOB_SCHEDULER_START_COMMAND;
        }

        @Override
        public int scheduleIdForWaitNet() {
            return JOB_SCHEDULER_WAIT_NET;
        }

        @Override
        public int waitNetMinimumLatency() {
            return 60_000; // 60秒
        }

        @Override
        public int waitNetOverrideDeadline() {
            return 60 * 60_000; // 一个小时唤醒一次
        }
    }
}
