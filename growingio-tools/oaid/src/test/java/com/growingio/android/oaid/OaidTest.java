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
package com.growingio.android.oaid;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.utils.OaidHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OaidTest {
    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void crash() {
        OaidLibraryGioModule module = new OaidLibraryGioModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(context, trackerRegistry);
        trackerRegistry.register(OaidHelper.class, String.class, new OaidDataLoader.Factory(context));
        ModelLoader<OaidHelper, String> modelLoader = trackerRegistry.getModelLoader(OaidHelper.class, String.class);
        modelLoader.buildLoadData(new OaidHelper()).fetcher.loadData(new DataFetcher.DataCallback<String>() {
            @Override
            public void onDataReady(String data) {
                assertThat(data).isNull();
            }

            @Override
            public void onLoadFailed(Exception e) {

            }
        });
        modelLoader.buildLoadData(new OaidHelper()).fetcher.cleanup();
        modelLoader.buildLoadData(new OaidHelper()).fetcher.cancel();
        assertThat(String.class).isEqualTo(modelLoader.buildLoadData(new OaidHelper()).fetcher.getDataClass());

    }
}
