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

/**
 * <p>
 *
 * @author cpacm 2022/8/2
 */
public class AdsResult {
    private boolean hasDealWithDeepLink = false;

    public AdsResult() {
    }

    public AdsResult(boolean hasDealWithDeepLink) {
        this.hasDealWithDeepLink = hasDealWithDeepLink;
    }

    public boolean hasDealWithDeepLink() {
        return hasDealWithDeepLink;
    }

    public void setHasDealWithDeepLink(boolean hasDealWithDeepLink) {
        this.hasDealWithDeepLink = hasDealWithDeepLink;
    }
}
