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

package com.growingio.android.sdk.track.async;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class HandlerDisposable implements Disposable {
    private final Handler mHandler;
    private volatile boolean mDisposed;

    public HandlerDisposable() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public Disposable schedule(Runnable run, long timeout) {
        if (mDisposed) {
            return EmptyDisposable.INSTANCE;
        }

        ScheduledRunnable scheduled = new ScheduledRunnable(mHandler, run);

        Message message = Message.obtain(mHandler, scheduled);
        message.obj = this;

        mHandler.sendMessageDelayed(message, timeout);

        return scheduled;
    }

    @Override
    public void dispose() {
        mDisposed = true;
        mHandler.removeCallbacksAndMessages(this);
    }

    @Override
    public boolean isDisposed() {
        return mDisposed;
    }

    private static final class ScheduledRunnable implements Runnable, Disposable {
        private final Handler mScheduledHandler;
        private final Runnable mDelegate;

        private volatile boolean mScheduledDisposed; // Tracked solely for isDisposed().

        ScheduledRunnable(Handler handler, Runnable delegate) {
            mScheduledHandler = handler;
            mDelegate = delegate;
        }

        @Override
        public void run() {
            mScheduledDisposed = true;
            mDelegate.run();
        }

        @Override
        public void dispose() {
            mScheduledHandler.removeCallbacks(this);
            mScheduledDisposed = true;
        }

        @Override
        public boolean isDisposed() {
            return mScheduledDisposed;
        }
    }
}
