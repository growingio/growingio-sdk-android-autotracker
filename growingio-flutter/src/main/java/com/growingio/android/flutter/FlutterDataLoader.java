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
package com.growingio.android.flutter;


import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.EventFlutter;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 2022/9/27
 */
public class FlutterDataLoader implements ModelLoader<EventFlutter, Void> {

    private final TrackerContext context;
    private final FlutterPluginProvider flutterPluginProvider;

    public FlutterDataLoader(TrackerContext context) {
        this.context = context;
        flutterPluginProvider = context.getProvider(FlutterPluginProvider.class);
    }

    @Override
    public LoadData<Void> buildLoadData(EventFlutter eventFlutter) {
        return new LoadData<>(new DataFetcher<Void>() {
            @Override
            public Void executeData() {
                if (eventFlutter.isCircleEnabled()) {
                    flutterPluginProvider.startFlutterCircle();
                } else if (eventFlutter.isDebuggerEnabled()) {
                    flutterPluginProvider.startFlutterDebugger();
                } else {
                    flutterPluginProvider.stopFlutterCircle();
                    flutterPluginProvider.stopFlutterDebugger();
                }
                return null;
            }

            @Override
            public Class<Void> getDataClass() {
                return Void.class;
            }
        });
    }

    public static class Factory implements ModelLoaderFactory<EventFlutter, Void> {

        private final TrackerContext trackerContext;

        protected Factory(TrackerContext context) {
            this.trackerContext = context;
        }

        @Override
        public ModelLoader<EventFlutter, Void> build() {
            return new FlutterDataLoader(trackerContext);
        }
    }
}
