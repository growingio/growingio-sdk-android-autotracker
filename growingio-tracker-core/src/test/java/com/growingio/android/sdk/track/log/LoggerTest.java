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

package com.growingio.android.sdk.track.log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class LoggerTest {

    @Test
    public void testLog() {
        Logger.v("tag", "message");
        Logger.v("tag", new Exception("exception message"));
        Logger.v("tag", new Exception("exception message"), "message");
        Logger.d("tag", "message");
        Logger.d("tag", new Exception("exception message"));
        Logger.d("tag", new Exception("exception message"), "message");
        Logger.i("tag", "message");
        Logger.i("tag", new Exception("exception message"));
        Logger.i("tag", new Exception("exception message"), "message");
        Logger.w("tag", "message");
        Logger.w("tag", new Exception("exception message"));
        Logger.w("tag", new Exception("exception message"), "message");
        Logger.e("tag", "message");
        Logger.e("tag", new Exception("exception message"));
        Logger.e("tag", new Exception("exception message"), "message");
        Logger.wtf("tag", "message");
        Logger.wtf("tag", new Exception("exception message"));
        Logger.wtf("tag", new Exception("exception message"), "message");
        Logger.printJson("tag", "headString", "{\"name\": \"BeJson\"}");
    }
}
