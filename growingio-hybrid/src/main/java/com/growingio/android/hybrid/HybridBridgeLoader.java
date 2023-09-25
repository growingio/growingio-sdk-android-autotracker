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
package com.growingio.android.hybrid;

import android.webkit.WebView;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.middleware.hybrid.HybridBridge;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class HybridBridgeLoader implements ModelLoader<HybridBridge, Boolean> {

    private final HybridBridgeProvider hybridBridgeProvider;

    public HybridBridgeLoader(TrackerContext context) {
        hybridBridgeProvider = context.getProvider(HybridBridgeProvider.class);
    }

    @Override
    public LoadData<Boolean> buildLoadData(HybridBridge eventData) {
        return new LoadData<>(new HybridDataFetcher(eventData, hybridBridgeProvider));
    }

    public static class Factory implements ModelLoaderFactory<HybridBridge, Boolean> {

        private final TrackerContext context;

        public Factory(TrackerContext context) {
            this.context = context;
        }

        @Override
        public ModelLoader<HybridBridge, Boolean> build() {
            return new HybridBridgeLoader(context);
        }
    }


    public static class HybridDataFetcher implements DataFetcher<Boolean> {

        private final HybridBridge bridge;
        private final HybridBridgeProvider hybridBridgeProvider;

        public HybridDataFetcher(HybridBridge eventData, HybridBridgeProvider hybridBridgeProvider) {
            this.bridge = eventData;
            this.hybridBridgeProvider = hybridBridgeProvider;
            this.hybridBridgeProvider.setDownGrade(bridge.isDownGrade());
        }


        @Override
        public Boolean executeData() {
            if (bridge.getView() instanceof WebView) {
                hybridBridgeProvider.bridgeForWebView(SuperWebView.make((WebView) bridge.getView()));
            } else if (ClassExistHelper.instanceOfX5WebView(bridge.getView())) {
                hybridBridgeProvider.bridgeForWebView(SuperWebView.makeX5((com.tencent.smtt.sdk.WebView) bridge.getView()));
            } else if (ClassExistHelper.instanceOfUcWebView(bridge.getView())) {
                hybridBridgeProvider.bridgeForWebView(SuperWebView.makeUC((com.uc.webview.export.WebView) bridge.getView()));
            } else {
                return false;
            }
            return true;
        }

        @Override
        public Class<Boolean> getDataClass() {
            return Boolean.class;
        }

    }
}
