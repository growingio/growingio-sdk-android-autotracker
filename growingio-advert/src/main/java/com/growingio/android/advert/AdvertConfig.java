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

package com.growingio.android.advert;

import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.track.middleware.advert.DeepLinkCallback;

/**
 * <p>
 *
 * @author cpacm 2022/8/3
 */
public class AdvertConfig implements Configurable {

    private boolean readClipBoardEnable = true;
    private String deepLinkHost;
    private DeepLinkCallback deepLinkCallback;

    public AdvertConfig setReadClipBoardEnable(boolean readClipBoardEnable) {
        this.readClipBoardEnable = readClipBoardEnable;
        return this;
    }

    public AdvertConfig setDeepLinkCallback(DeepLinkCallback advertReceiveCallback) {
        this.deepLinkCallback = advertReceiveCallback;
        return this;
    }

    public String getDeepLinkHost() {
        return deepLinkHost;
    }

    public AdvertConfig setDeepLinkHost(String deeplinkHost) {
        if (deeplinkHost == null || deeplinkHost.isEmpty()) {
            throw new IllegalArgumentException("deepLink must not be empty");
        }
        if (!deeplinkHost.startsWith("http")) {
            deeplinkHost = "https://" + deeplinkHost;
        }
        this.deepLinkHost = deeplinkHost;
        return this;
    }

    public boolean isReadClipBoardEnable() {
        return readClipBoardEnable;
    }

    public DeepLinkCallback getDeepLinkCallback() {
        return deepLinkCallback;
    }
}
