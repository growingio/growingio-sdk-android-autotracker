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
package com.growingio.android.sdk.track.middleware.abtest;

public class ABTest {
    final String layerId;

    final ABTestCallback abTestCallback;

    final boolean requestImmediately;

    public ABTest(String layerId, ABTestCallback abTestCallback) {
        this.layerId = layerId;
        this.abTestCallback = abTestCallback;
        this.requestImmediately = false;
    }

    public ABTest(String layerId, ABTestCallback abTestCallback, boolean immediately) {
        this.layerId = layerId;
        this.abTestCallback = abTestCallback;
        this.requestImmediately = immediately;
    }

    public String getLayerId() {
        return layerId;
    }

    public ABTestCallback getAbTestCallback() {
        return abTestCallback;
    }

    public boolean isRequestImmediately() {
        return requestImmediately;
    }
}
