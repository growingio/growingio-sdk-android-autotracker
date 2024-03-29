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
package com.growingio.android.sdk.track.middleware.ads;

import android.net.Uri;

/**
 * <p>
 * try send Activate Event.
 *
 * @author cpacm 5/17/21
 */
public class Activate {

    final Uri uri;
    final DeepLinkCallback callback;

    private Activate(Uri uri, DeepLinkCallback callback) {
        this.uri = uri;
        this.callback = callback;
    }

    public Uri getUri() {
        return uri;
    }

    public DeepLinkCallback getCallback() {
        return callback;
    }

    public static Activate activate() {
        return new Activate(null, null);
    }

    public static Activate deeplink(Uri uri) {
        return new Activate(uri, null);
    }

    public static Activate handleDeeplink(Uri uri, DeepLinkCallback callback) {
        return new Activate(uri, callback);
    }


}
