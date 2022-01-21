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

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.utils.OaidHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Thread.sleep;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OaidTest {
    private final Application context = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(context);
    }

    @Test
    public void oaid() {
        OaidLibraryGioModule module = new OaidLibraryGioModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(context, trackerRegistry);
        trackerRegistry.register(OaidHelper.class, String.class, new OaidDataLoader.Factory(context));
        ModelLoader<OaidHelper, String> modelLoader = trackerRegistry.getModelLoader(OaidHelper.class, String.class);
        assertThat(String.class).isEqualTo(modelLoader.buildLoadData(new OaidHelper()).fetcher.getDataClass());
    }

    @Test
    public void oaidConfig1() {
        TrackerRegistry trackerRegistry = TrackerContext.get().getRegistry();
        OaidConfig config = new OaidConfig().setProvideOaid("cpacm");
        trackerRegistry.register(OaidHelper.class, String.class, new OaidDataLoader.Factory(context, config));
        ModelLoader<OaidHelper, String> modelLoader = trackerRegistry.getModelLoader(OaidHelper.class, String.class);
        String oaid = modelLoader.buildLoadData(new OaidHelper()).fetcher.executeData();
        assertThat(oaid).isEqualTo("cpacm");
    }

    @Test
    public void oaidConfig2() {
        TrackerRegistry trackerRegistry = TrackerContext.get().getRegistry();
        OaidConfig config = new OaidConfig().setProvideOaidCallback(context -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "cpacm_job";
        });
        trackerRegistry.register(OaidHelper.class, String.class, new OaidDataLoader.Factory(context, config));
        ModelLoader<OaidHelper, String> modelLoader = trackerRegistry.getModelLoader(OaidHelper.class, String.class);
        String oaid = modelLoader.buildLoadData(new OaidHelper()).fetcher.executeData();
        assertThat(oaid).isNull();
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        oaid = modelLoader.buildLoadData(new OaidHelper()).fetcher.executeData();
        assertThat(oaid).isEqualTo("cpacm_job");
    }

    @Test
    public void oaidConfig3() {
        TrackerRegistry trackerRegistry = TrackerContext.get().getRegistry();
        OaidConfig config = new OaidConfig().setProvideCert("UNKNOWN CERT");
        trackerRegistry.register(OaidHelper.class, String.class, new OaidDataLoader.Factory(context, config));
        ModelLoader<OaidHelper, String> modelLoader = trackerRegistry.getModelLoader(OaidHelper.class, String.class);
        String oaid = modelLoader.buildLoadData(new OaidHelper()).fetcher.executeData();
        assertThat(oaid).isNull();
    }
}
