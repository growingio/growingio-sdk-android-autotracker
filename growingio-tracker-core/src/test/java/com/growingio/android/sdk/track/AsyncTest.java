/*
 *
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.growingio.android.sdk.track;


import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.async.HandlerDisposable;
import com.growingio.android.sdk.track.async.UnsubscribedDisposable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AsyncTest {

    @Test
    public void handlerDisposableTest() {
        HandlerDisposable hd = new HandlerDisposable();
        Disposable d = hd.schedule(hd::dispose, 1000);
        Truth.assertThat(d.isDisposed()).isFalse();
        Truth.assertThat(hd.isDisposed()).isFalse();
        //ShadowLooper.runUiThreadTasks();
        Robolectric.flushForegroundThreadScheduler();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Truth.assertThat(d.isDisposed()).isTrue();
        Disposable empty = hd.schedule(() -> {
        }, 1000);
        Truth.assertThat(empty.isDisposed()).isTrue();
    }

    @Test
    public void unsubscribedTest() {
        UnsubscribedDisposable ud = new UnsubscribedDisposable();
        Truth.assertThat(ud.isDisposed()).isFalse();
        ud.dispose();
        Truth.assertThat(ud.isDisposed()).isTrue();
    }
}
