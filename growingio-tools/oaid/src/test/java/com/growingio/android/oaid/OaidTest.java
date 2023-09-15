/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.OaidHelper;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OaidTest {
    private final Application application = ApplicationProvider.getApplicationContext();

    private TrackerContext context;

    @Before
    public void setup() {
        TrackerLifecycleProviderFactory.create()
                .createConfigurationProviderWithConfig(new CoreConfiguration("OaidTest", "growingio://oaid"), new HashMap<>());
        Tracker tracker = new Tracker(application);
        tracker.registerComponent(new OaidLibraryGioModule());
        context = tracker.getContext();
    }

    @Test
    public void oaid() {
        ModelLoader<OaidHelper, String> modelLoader = context.getRegistry().getModelLoader(OaidHelper.class, String.class);
        assertThat(String.class).isEqualTo(modelLoader.buildLoadData(new OaidHelper()).fetcher.getDataClass());
    }

    @Test
    public void oaidConfig1() {
        OaidConfig config = new OaidConfig().setProvideOaid("cpacm");
        context.getConfigurationProvider().addConfiguration(config);
        ModelLoader<OaidHelper, String> modelLoader = context.getRegistry().getModelLoader(OaidHelper.class, String.class);
        String oaid = modelLoader.buildLoadData(new OaidHelper()).fetcher.executeData();
        assertThat(oaid).isEqualTo("cpacm");
    }

    @Test
    public void oaidConfig3() {
        OaidConfig config = new OaidConfig().setProvideCert("UNKNOWN CERT");
        context.getConfigurationProvider().addConfiguration(config);
        ModelLoader<OaidHelper, String> modelLoader = context.getRegistry().getModelLoader(OaidHelper.class, String.class);
        String oaid = modelLoader.buildLoadData(new OaidHelper()).fetcher.executeData();
        assertThat(oaid).isNull();
    }
}
