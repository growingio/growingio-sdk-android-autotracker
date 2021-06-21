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
package com.growingio.android.crash;

import com.growingio.android.sdk.Configurable;

/**
 * <p>
 *
 * @author cpacm 2021/6/1
 */
public class CrashConfig implements Configurable {
    public static final String ALIAS = "gandroid";
    /**
     * https://sentry.growingio.com/settings/growingio/projects/gioandroid/keys/ 查看对应DSN
     */
    private static final String DSN = "https://3db1f04a7238465ab285f46b462b5ffe@sentry.growingio.com/7";


    private String crashDsn = DSN;
    private String crashAlias = ALIAS;

    public String getCrashDsn() {
        return crashDsn;
    }

    public CrashConfig setCrashDsn(String crashDsn) {
        this.crashDsn = crashDsn;
        return this;
    }

    public String getCrashAlias() {
        return crashAlias;
    }

    public CrashConfig setCrashAlias(String crashAlias) {
        this.crashAlias = crashAlias;
        return this;
    }
}
