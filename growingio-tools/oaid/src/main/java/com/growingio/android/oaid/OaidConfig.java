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

package com.growingio.android.oaid;


import android.content.Context;

import com.growingio.android.sdk.Configurable;

/**
 * <p>
 * 请选择其中一种方式传入oaid，若多处设置，则按照以下优先级获取
 * provideOaid -> OnProvideOaidCallback
 * 请提供oaid需要的证书，默认将从asset下获取 context.getPackageName() + ".cert.pem" 名称的证书。若多处设置，则按照以下优先级获取
 * provideCert -> provideCertAsset -> OnProvideCertCallback -> 默认
 *
 * @author cpacm 2022/1/14
 */
public class OaidConfig implements Configurable {
    private String provideOaid; //外部提供的oaid
    private OnProvideOaidCallback provideOaidCallback;

    private String provideCert;
    private String provideCertAsset;
    private OnProvideCertCallback provideCertCallback;

    protected String getProvideOaid() {
        return provideOaid;
    }

    public OaidConfig setProvideOaid(String provideOaid) {
        this.provideOaid = provideOaid;
        return this;
    }

    protected String getProvideCert() {
        return provideCert;
    }

    public OaidConfig setProvideCert(String provideCert) {
        this.provideCert = provideCert;
        return this;
    }

    protected String getProvideCertAsset() {
        return provideCertAsset;
    }

    public OaidConfig setProvideCertAsset(String provideCertAsset) {
        this.provideCertAsset = provideCertAsset;
        return this;
    }

    protected OnProvideOaidCallback getProvideOaidCallback() {
        return provideOaidCallback;
    }

    public OaidConfig setProvideOaidCallback(OnProvideOaidCallback provideOaidCallback) {
        this.provideOaidCallback = provideOaidCallback;
        return this;
    }

    protected OnProvideCertCallback getProvideCertCallback() {
        return provideCertCallback;
    }

    public OaidConfig setProvideCertCallback(OnProvideCertCallback provideCertCallback) {
        this.provideCertCallback = provideCertCallback;
        return this;
    }

    public interface OnProvideOaidCallback {
        String provideOaidJob(Context context);
    }

    public interface OnProvideCertCallback {
        String provideCertJob(Context context);
    }

}
