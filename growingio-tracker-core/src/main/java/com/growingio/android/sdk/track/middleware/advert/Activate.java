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

package com.growingio.android.sdk.track.middleware.advert;

import android.net.Uri;

/**
 * <p>
 * try send Activate Event.
 *
 * @author cpacm 5/17/21
 */
public class Activate {

    final Uri uri;
    final boolean dataSwitch;

    public Activate(Uri uri) {
        this.uri = uri;
        this.dataSwitch = false;
    }

    public Activate(Uri uri, boolean dataSwitch) {
        this.uri = uri;
        this.dataSwitch = dataSwitch;
    }

    public Uri getUri() {
        return uri;
    }

    public boolean isDataSwitch() {
        return dataSwitch;
    }
}
