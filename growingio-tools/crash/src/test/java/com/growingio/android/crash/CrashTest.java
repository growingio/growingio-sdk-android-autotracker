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
package com.growingio.android.crash;


import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.monitor.event.EventBuilder;
import com.growingio.android.sdk.track.log.Crash;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CrashTest {
    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void crash() {
        CrashLibraryGioModule module = new CrashLibraryGioModule();
        assertThat(CrashManager.isEnabled()).isFalse();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(context, trackerRegistry);
        assertThat(CrashManager.isEnabled()).isTrue();
        CrashConfig crashConfig = new CrashConfig();
        crashConfig.setCrashAlias("").setCrashDsn("");

        trackerRegistry.register(Crash.class, Void.class, new CrashDataLoader.Factory(context, CrashConfig.DSN, CrashConfig.ALIAS));
        ModelLoader<Crash, Void> modelLoader = trackerRegistry.getModelLoader(Crash.class, Void.class);
        modelLoader.buildLoadData(new Crash()).fetcher.executeData();
        modelLoader.buildLoadData(new Crash()).fetcher.loadData(new DataFetcher.DataCallback<Void>() {
            @Override
            public void onDataReady(Void data) {
                assertThat(data).isNull();
            }

            @Override
            public void onLoadFailed(Exception e) {

            }
        });
        modelLoader.buildLoadData(new Crash()).fetcher.cleanup();
        modelLoader.buildLoadData(new Crash()).fetcher.cancel();
        assertThat(Void.class).isEqualTo(modelLoader.buildLoadData(new Crash()).fetcher.getDataClass());
        CrashManager.sendMessage("cpacm");
        CrashManager.sendException(new IllegalStateException("cpacm"));
        CrashManager.sendEvent(new EventBuilder().withMessage("cpacm"));
        CrashManager.close();
        CrashManager.unRegister();
        assertThat(CrashManager.isEnabled()).isFalse();
    }

    @Test
    public void crashLogger() {
        Logger.addLogger(new CrashLogger());
        Logger.i("CrashTest", "crash[i]");
        Logger.w("CrashTest", "crash[w]");
        Logger.v("CrashTest", "crash[v]");
        Logger.d("CrashTest", "crash[d]");
        Logger.e("CrashTest", "crash[e]");
        Logger.wtf("CrashTest", "crash[wtf]");

    }

}
