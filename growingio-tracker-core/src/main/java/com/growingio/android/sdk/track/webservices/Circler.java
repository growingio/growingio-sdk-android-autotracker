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

package com.growingio.android.sdk.track.webservices;

import java.util.Map;

/**
 * <p>
 *     start circler module.
 * @author cpacm 5/10/21
 */
public class Circler {

    final Map<String, String> params;

    public Circler(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder urlBuilder = new StringBuilder();
        if (!params.isEmpty()) {
            for (String key : params.keySet()) {
                urlBuilder.append(key).append("=").append(params.get(key)).append("\n");
            }
        }
        return urlBuilder.toString();
    }
}
