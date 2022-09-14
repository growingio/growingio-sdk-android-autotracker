/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.advert;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.advert.Activate;
import com.growingio.android.sdk.track.middleware.advert.AdvertResult;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ActivateTest {
    private Application context = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(context);
    }


    @Test
    public void activate() {

        AdvertLibraryGioModule module = new AdvertLibraryGioModule();
        TrackerRegistry trackerRegistry = TrackerContext.get().getRegistry();
        module.registerComponents(context, trackerRegistry);

        boolean isActivated = AdvertUtils.isDeviceActivated();
        Truth.assertThat(isActivated).isFalse();

        TrackerContext.get().executeData(new Activate(null, true), Activate.class, AdvertResult.class);

        Truth.assertThat(AdvertUtils.isDeviceActivated()).isTrue();

    }
}
