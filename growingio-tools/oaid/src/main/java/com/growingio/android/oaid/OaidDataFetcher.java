/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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

package com.growingio.android.oaid;

import android.content.Context;

import com.growingio.android.sdk.track.modelloader.DataFetcher;

public class OaidDataFetcher implements DataFetcher<String> {
    private static final String TAG = "OaidDataFetcher";
    private final IOaidHelper mOaidHelper;

    public OaidDataFetcher(Context context, IOaidHelper oaidHelper) {
        this.mOaidHelper = oaidHelper;
    }


    @Override
    public String executeData() {
        return mOaidHelper.getOaid();
    }

    @Override
    public Class<String> getDataClass() {
        return String.class;
    }
}
