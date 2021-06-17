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

package com.growingio.android.sdk;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class ModelLoaderTest {

    @Test
    public void registryTest() {
        TrackerRegistry registry = new TrackerRegistry();
        Truth.assertThat(registry.getModelLoader(String.class)).isEqualTo(null);
        ModelLoader<String, Integer> modelLoader = new TestModelLoader();
        TestModelLoaderFactory factory1 = new TestModelLoaderFactory(modelLoader);
        registry.register(String.class, Integer.class, factory1);
        Truth.assertThat(modelLoader).isEqualTo(registry.getModelLoader(String.class));
        int result = modelLoader.buildLoadData("cpacm").fetcher.executeData();
        Truth.assertThat(result).isEqualTo(0);

        ModelLoader<String, Integer> modelLoader2 = new TestModelLoader();
        TestModelLoaderFactory factory2 = new TestModelLoaderFactory(modelLoader2);
        registry.register(String.class, Integer.class, factory2);
        Truth.assertThat(modelLoader2).isEqualTo(registry.getModelLoader(String.class, Integer.class));
    }

    static class TestModelLoaderFactory implements ModelLoaderFactory<String, Integer> {

        private final ModelLoader<String, Integer> modelLoader;

        public TestModelLoaderFactory(ModelLoader<String, Integer> modelLoader) {
            this.modelLoader = modelLoader;
        }

        @Override
        public ModelLoader<String, Integer> build() {
            return modelLoader;
        }

    }

    static class TestModelLoader implements ModelLoader<String, Integer> {
        @Override
        public LoadData<Integer> buildLoadData(String s) {
            return new LoadData<>(new DataFetcher<Integer>() {
                @Override
                public void loadData(DataCallback<? super Integer> callback) {
                    callback.onDataReady(0);
                }

                @Override
                public Integer executeData() {
                    return 0;
                }

                @Override
                public void cleanup() {

                }

                @Override
                public void cancel() {

                }

                @Override
                public Class<Integer> getDataClass() {
                    return null;
                }
            });
        }
    }


}
