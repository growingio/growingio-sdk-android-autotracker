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

package com.growingio.android.sdk;

import android.content.Context;

import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

/**
 * Registers a set of components to use when initializing GrowingIO whthin an library when GrowingIO's
 * annotation processor is used.
 *
 * @author cpacm 4/23/21
 */
public abstract class LibraryGioModule {

    public void registerComponents(Context context, TrackerRegistry registry) {
    }

    protected <T> T getConfiguration(Class<? extends Configurable> clazz) {
        return ConfigurationProvider.get().getConfiguration(clazz);
    }
}
