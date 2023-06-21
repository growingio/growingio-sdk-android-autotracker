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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2021/4/1
 */
class ModelLoaderRegistry {

    private final Map<Class<?>, ModelLoader<?, ?>> cachedModelLoaders = new HashMap<>();
    private final List<Entry<?, ?>> entries = new ArrayList<>();

    public synchronized <Model, Data> void remove(Class<Model> modelClass, Class<Data> dataClass) {
        for (Iterator<Entry<?, ?>> iterator = entries.iterator(); iterator.hasNext();) {
            Entry<?, ?> entry = iterator.next();
            if (entry.handles(modelClass, dataClass)) {
                iterator.remove();
            }
        }
        cachedModelLoaders.remove(modelClass);
    }

    public synchronized <Model, Data> void put(Class<Model> modelClass, Class<Data> dataClass, ModelLoaderFactory<? extends Model, ? extends Data> factory) {
        add(modelClass, dataClass, factory);
        cachedModelLoaders.clear();
    }

    @SuppressWarnings("unchecked")
    public synchronized <Model> ModelLoader<Model, ?> getModelLoaderForClass(Class<Model> modelClass) {
        ModelLoader<?, ?> loader = cachedModelLoaders.get(modelClass);
        if (loader == null) {
            loader = this.build(modelClass);
        }
        if (loader == null) return null;
        else {
            cachedModelLoaders.put(modelClass, loader);
            return (ModelLoader<Model, ?>) loader;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized <Model, Data> ModelLoader<Model, Data> getModelLoader(Class<Model> modelClass, Class<Data> dataClass) {
        ModelLoader<?, ?> loader = cachedModelLoaders.get(modelClass);
        if (loader == null) {
            loader = this.build(modelClass);
        }
        if (loader == null) return null;
        else {
            cachedModelLoaders.put(modelClass, loader);
            return (ModelLoader<Model, Data>) loader;
        }
    }

    private <Model, Data> void add(
            Class<Model> modelClass,
            Class<Data> dataClass,
            ModelLoaderFactory<? extends Model, ? extends Data> factory) {
        for (Iterator<Entry<?, ?>> iterator = entries.iterator(); iterator.hasNext();) {
            Entry<?, ?> entry = iterator.next();
            if (entry.handles(modelClass, dataClass)) {
                iterator.remove();
            }
        }
        Entry<Model, Data> entry = new Entry<>(modelClass, dataClass, factory);
        entries.add(entry);
    }

    private synchronized <Model> ModelLoader<?, ?> build(Class<Model> modelClass) {
        for (Entry<?, ?> entry : entries) {
            if (entry.handles(modelClass)) {
                return entry.factory.build();
            }
        }
        return null;
    }


    private static class Entry<Model, Data> {
        private final Class<Model> modelClass;
        final Class<Data> dataClass;
        final ModelLoaderFactory<? extends Model, ? extends Data> factory;

        Entry(
                Class<Model> modelClass,
                Class<Data> dataClass,
                ModelLoaderFactory<? extends Model, ? extends Data> factory) {
            this.modelClass = modelClass;
            this.dataClass = dataClass;
            this.factory = factory;
        }

        public boolean handles(Class<?> modelClass, Class<?> dataClass) {
            return handles(modelClass) && this.dataClass.isAssignableFrom(dataClass);
        }

        public boolean handles(Class<?> modelClass) {
            return this.modelClass.isAssignableFrom(modelClass);
        }
    }
}
