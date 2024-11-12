/*
 *  Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
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
 */

package com.growingio.android.hybrid;

import com.growingio.android.sdk.Configurable;

public class HybridConfig implements Configurable {

    private boolean autoJsSdkInject = false;

    public boolean isAutoGrowingJsSdk() {
        return autoJsSdkInject;
    }

    public HybridConfig setAutoJsSdkInject(boolean autoJsSdkInject) {
        this.autoJsSdkInject = autoJsSdkInject;
        return this;
    }
}
