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

package com.growingio.android.sdk.track.utils;

import android.os.Handler;
import android.os.SystemClock;

/**
 * 时间过滤触发器
 * 外部触发条件过于频繁, 这里予以过滤
 */
public class TimerToggler implements Runnable {

    private final Handler mHandler;
    private final Runnable mAction;

    // other - 未触发的事件首次时间, -1 代表首次, 0 代表至少触发过一次,
    private long mFirstToggleTime = -1;

    private long mDelayTime;
    private long mMaxDelayTime;
    private boolean mFirstTimeDelay;

    private TimerToggler(Runnable action) {
        mHandler = new Handler();
        mAction = action;
    }

    public void toggle() {
        long currentTime = SystemClock.uptimeMillis();
        if (mDelayTime == 0) {
            // 没有设置延时时间
            takeAction();
        } else if (mFirstToggleTime == -1 && !mFirstTimeDelay) {
            // 首次， 不延时
            takeAction();
        } else if (mFirstToggleTime > 0 && currentTime - mFirstToggleTime >= mMaxDelayTime) {
            // 超过最大延时时间
            takeAction();
        } else {
            if (mFirstToggleTime <= 0) {
                mFirstToggleTime = currentTime;
            }
            mHandler.removeCallbacks(this);
            long targetTime = Math.min(mFirstToggleTime + mMaxDelayTime, currentTime + mDelayTime);
            mHandler.postAtTime(this, targetTime);
        }
    }

    void takeAction() {
        mHandler.removeCallbacks(this);
        mFirstToggleTime = 0;
        mAction.run();
    }

    public void reset() {
        mFirstToggleTime = -1;
        mHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        takeAction();
    }

    public static class Builder {
        private long mDelayTime = 50;
        private long mMaxDelayTime = 600;
        private final Runnable mAction;
        private boolean mFirstTimeDelay = true;

        public Builder(Runnable action) {
            this.mAction = action;
        }

        /**
         * 触发添加的延时时间
         */
        public Builder delayTime(long delayTime) {
            this.mDelayTime = delayTime;
            return this;
        }

        /**
         * 延时到触发的最大延时时间
         */
        public Builder maxDelayTime(long maxDelayTime) {
            this.mMaxDelayTime = maxDelayTime;
            return this;
        }

        /**
         * @param firstTimeDelay true 首次触发有延时处理
         */
        public Builder firstTimeDelay(boolean firstTimeDelay) {
            this.mFirstTimeDelay = firstTimeDelay;
            return this;
        }

        public TimerToggler build() {
            TimerToggler toggler = new TimerToggler(this.mAction);
            toggler.mMaxDelayTime = mMaxDelayTime;
            toggler.mDelayTime = mDelayTime;
            toggler.mFirstTimeDelay = mFirstTimeDelay;
            return toggler;
        }
    }
}
