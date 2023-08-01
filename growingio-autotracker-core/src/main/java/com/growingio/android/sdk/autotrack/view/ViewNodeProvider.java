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
package com.growingio.android.sdk.autotrack.view;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.track.middleware.hybrid.HybridDom;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.view.DecorView;

import org.json.JSONArray;

import java.util.List;

/**
 * <p>
 * calculate view node
 *
 * @author cpacm 2023/7/7
 */
public class ViewNodeProvider implements ViewNodeRenderer, TrackerLifecycleProvider {

    private final static String TAG = "ViewNodeProvider";

    private ViewNodeRenderer renderer;
    private TrackerRegistry registry;

    public ViewNodeProvider() {

    }

    @Override
    public void setup(TrackerContext context) {
        registry = context.getRegistry();
        AutotrackConfig config = context.getConfigurationProvider().getConfiguration(AutotrackConfig.class);
        boolean isV4 = config == null || !config.isDowngrade();
        if (isV4) {
            renderer = new ViewNodeV4Renderer(this);
        } else {
            renderer = new ViewNodeV3Renderer(this);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void generateMenuItemEvent(Activity activity, MenuItem menuItem) {
        renderer.generateMenuItemEvent(activity, menuItem);
    }

    @Override
    public void generateViewClickEvent(View view) {
        renderer.generateViewClickEvent(view);
    }

    @Override
    public void generateViewChangeEvent(View view) {
        renderer.generateViewChangeEvent(view);
    }

    @Override
    public JSONArray buildScreenPages(List<DecorView> decorViews) {
        return renderer.buildScreenPages(decorViews);
    }

    @Override
    public JSONArray buildScreenViews(List<DecorView> decorViews) {
        return renderer.buildScreenViews(decorViews);
    }

    ModelLoader<HybridDom, HybridJson> getHybridModelLoader() {
        return registry.getModelLoader(HybridDom.class, HybridJson.class);
    }
}
