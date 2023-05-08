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

package com.growingio.autotest.help;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.middleware.EventSender;

import org.powermock.reflect.Whitebox;

import static androidx.test.espresso.Espresso.onIdle;

public class TrackHelper {
    private TrackHelper() {
    }

    public static void waitForIdleSync() {
        waitUiThreadForIdleSync();
        waitTrackMainThreadForIdleSync();
        waitEventSendThreadForIdleSync();
    }

    public static void waitUiThreadForIdleSync() {
        onIdle();
    }

    public static void waitTrackMainThreadForIdleSync() {
        Looper mainLooper = TrackMainThread.trackMain().getMainHandler().getLooper();
        if (Looper.myLooper() == mainLooper) {
            throw new RuntimeException(
                    "This method can not be called from the TrackMainThread");
        }
        Handler handler = Whitebox.getInternalState(TrackMainThread.trackMain(), "mMainHandler");
        Idler idler = new Idler(null);
        mainLooper.getQueue().addIdleHandler(idler);
        handler.post(new EmptyRunnable());
        idler.waitForIdle();
    }

    public static void waitEventSendThreadForIdleSync() {
        EventSender eventSender = Whitebox.getInternalState(TrackMainThread.trackMain(), "mEventSender");
        Handler sendHandler = Whitebox.getInternalState(eventSender, "mSendHandler");
        if (Looper.myLooper() == sendHandler.getLooper()) {
            throw new RuntimeException(
                    "This method can not be called from the EventSendThread");
        }
        Idler idler = new Idler(null);
        sendHandler.getLooper().getQueue().addIdleHandler(idler);
        sendHandler.post(new EmptyRunnable());
        idler.waitForIdle();
    }

    public static void postToUiThread(Runnable run) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(run);
    }

    private static final class EmptyRunnable implements Runnable {
        @Override
        public void run() {
        }
    }

    private static final class Idler implements MessageQueue.IdleHandler {
        private final Runnable mCallback;
        private boolean mIdle;

        Idler(Runnable callback) {
            mCallback = callback;
            mIdle = false;
        }

        @Override
        public boolean queueIdle() {
            if (mCallback != null) {
                mCallback.run();
            }
            synchronized (this) {
                mIdle = true;
                notifyAll();
            }
            return false;
        }

        public void waitForIdle() {
            synchronized (this) {
                while (!mIdle) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
