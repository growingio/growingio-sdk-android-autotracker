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

package com.growingio.android.flutter;

import android.content.Context;

import com.growingio.android.sdk.track.middleware.EventFlutter;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 * when register apm module,init gmonitor sdk.
 *
 * @author cpacm 2022/9/27
 */
public class FlutterDataLoader implements ModelLoader<EventFlutter, Void> {

    @Override
    public LoadData<Void> buildLoadData(EventFlutter eventFlutter) {
        return new LoadData<>(new DataFetcher<Void>() {
            @Override
            public Void executeData() {
                if (eventFlutter.isCircleEnabled()) {
                    FlutterPluginProvider.get().startFlutterCircle();
                } else {
                    FlutterPluginProvider.get().stopFlutterCircle();
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

        protected Factory(Context context) {
        }

        @Override
        public ModelLoader<EventFlutter, Void> build() {
            return new FlutterDataLoader();
        }
    }
}
