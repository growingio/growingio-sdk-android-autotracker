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

/**
 * <p>
 *
 * @author cpacm 2022/8/3
 */
public class AdvertConfig implements Configurable {

    private boolean readClipBoardEnable = true;
    private AdvertReceiveCallback receiveCallback;

    public AdvertConfig setReadClipBoardEnable(boolean readClipBoardEnable) {
        this.readClipBoardEnable = readClipBoardEnable;
        return this;
    }

    public AdvertConfig setReceiveCallback(AdvertReceiveCallback advertReceiveCallback) {
        this.receiveCallback = advertReceiveCallback;
        return this;
    }

    public boolean isReadClipBoardEnable() {
        return readClipBoardEnable;
    }

    public AdvertReceiveCallback getReceiveCallback() {
        return receiveCallback;
    }
}
