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
package com.growingio.android.platform.harmony;
public class ConstantPool {
    private ConstantPool() {
    }

    public static final String HARMONY = "Harmony";

    public static final String HARMONY_VERSION_KEY = "hw_sc.build.platform.version";

    public static final String EXEC_CMD_GETPROP = "/system/bin/getprop";

    public static final String CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties";

    public static final String GET_METHOD = "get";
}