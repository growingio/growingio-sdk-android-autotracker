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
package com.growingio.android.sdk.track.middleware.webservice;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * start debugger module.
 *
 * @author cpacm 5/10/21
 */
public class Debugger {

    public final static int DEBUGGER_INIT = 0;
    public final static int DEBUGGER_SCREENSHOT = 1;
    public final static int DEBUGGER_REFRESH = 2;

    public final int debuggerDataType;

    private final Map<String, String> params;

    public Debugger(Map<String, String> params) {
        this.params = params;
        screenshot = null;
        debuggerDataType = DEBUGGER_INIT;
    }

    public Debugger(int type){
        params = new HashMap<>();
        screenshot = null;
        debuggerDataType = type;
    }

    private final byte[] screenshot;

    public Debugger(byte[] screenshot) {
        params = new HashMap<>();
        this.screenshot = screenshot;
        debuggerDataType = DEBUGGER_SCREENSHOT;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public byte[] getScreenshot() {
        return screenshot;
    }
}
