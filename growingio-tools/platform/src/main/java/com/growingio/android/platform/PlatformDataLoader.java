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
package com.growingio.android.platform;

import com.growingio.android.platform.harmony.HarmonyHandler;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.platform.PlatformHelper;
import com.growingio.android.sdk.track.middleware.platform.PlatformInfo;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

public class PlatformDataLoader implements ModelLoader<PlatformHelper, PlatformInfo> {
    private final TrackerContext context;

    PlatformDataLoader(TrackerContext context) {
        this.context = context;
    }

    @Override
    public LoadData<PlatformInfo> buildLoadData(PlatformHelper harmonyHelper) {
        return new LoadData<>(new PlatformDataFetcher(context));
    }

    static class Factory implements ModelLoaderFactory<PlatformHelper, PlatformInfo> {

        private final TrackerContext context;

        public Factory(TrackerContext context) {
            this.context = context;
        }

        @Override
        public ModelLoader<PlatformHelper, PlatformInfo> build() {
            return new PlatformDataLoader(context);
        }
    }
}
