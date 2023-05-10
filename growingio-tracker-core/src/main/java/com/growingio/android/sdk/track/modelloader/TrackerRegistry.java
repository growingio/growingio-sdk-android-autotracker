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

package com.growingio.android.sdk.track.modelloader;

/**
 * <p>
 * Manages component registration to extend or replace Tracker's default feature
 *
 * @author cpacm 2021/3/30
 */
public class TrackerRegistry {

    private final ModelLoaderRegistry modelLoaderRegistry;

    public TrackerRegistry() {
        modelLoaderRegistry = new ModelLoaderRegistry();
    }

    public <Model> ModelLoader<Model, ?> getModelLoader(Class<Model> modelClass) {
        return modelLoaderRegistry.getModelLoaderForClass(modelClass);
    }

    public <Model, Data> ModelLoader<Model, Data> getModelLoader(Class<Model> modelClass, Class<Data> dataClass) {
        return modelLoaderRegistry.getModelLoader(modelClass, dataClass);
    }

    public <Model, Data> TrackerRegistry register(Class<Model> modelClass, Class<Data> dataClass, ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.put(modelClass, dataClass, factory);
        return this;
    }

    /**
     * Unregister a {@link ModelLoaderFactory} that is capable of handling a specific model and data class.
     * Don't use in production environment,it's a test api.
     */
    public <Model,Data> void unregister(Class<Model> modelClass, Class<Data> dataClass){
        modelLoaderRegistry.remove(modelClass, dataClass);
    }
}
