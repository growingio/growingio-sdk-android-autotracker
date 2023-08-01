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
package com.growingio.android.sdk.track;


import android.util.Log;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.log.BaseLogger;
import com.growingio.android.sdk.track.log.CacheLogger;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.ILogger;
import com.growingio.android.sdk.track.log.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static android.util.Log.ASSERT;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class LogTest {

    private static final String TAG = "TEST";

    @Before
    public void setup() {
        //ShadowLog.stream = System.out;
    }


    @Test
    public void loggerTest() {
        ILogger log = new BaseLogger() {
            @Override
            protected void print(int priority, String tag, String message, Throwable t) {
                Truth.assertThat(tag).isEqualTo(TAG);
                switch (priority) {
                    case VERBOSE:
                        Log.v(tag, message);
                        if (t != null) {
                            Truth.assertThat(t).hasMessageThat().isEqualTo("v");
                        } else {
                            Truth.assertThat(message).isEqualTo("VERBOSE");
                        }
                        break;
                    case DEBUG:
                        Log.d(tag, message);
                        if (t != null) {
                            Truth.assertThat(t).hasMessageThat().isEqualTo("d");
                        } else {
                            Truth.assertThat(message).isEqualTo("DEBUG");
                        }
                        break;
                    case INFO:
                        Log.i(tag, message);
                        if (t != null) {
                            Truth.assertThat(t).hasMessageThat().isEqualTo("i");
                        } else {
                            Truth.assertThat(message).isEqualTo("INFO");
                        }
                        break;
                    case WARN:
                        Log.w(tag, message);
                        if (t != null) {
                            Truth.assertThat(t).hasMessageThat().isEqualTo("w");
                        } else {
                            Truth.assertThat(message).isEqualTo("WARN");
                        }
                        break;
                    case ERROR:
                        Log.e(tag, message);
                        if (t != null) {
                            Truth.assertThat(t).hasMessageThat().isEqualTo("e");
                        } else {
                            Truth.assertThat(message).isEqualTo("ERROR");
                        }
                        break;
                    case ASSERT:
                        Log.wtf(tag, message);
                        if (t != null) {
                            Truth.assertThat(t).hasMessageThat().isEqualTo("wtf");
                        } else {
                            Truth.assertThat(message).isEqualTo("ASSERT");
                        }
                        break;
                    default:
                        break;
                }

            }

            @Override
            public String getType() {
                return "TestLogger";
            }
        };
        Logger.addLogger(log);
        Logger.v(TAG, "%s", "VERBOSE");
        Logger.v(TAG, new NullPointerException("v"));
        Logger.v(TAG, new NullPointerException("v"), "VERBOSE");

        Logger.d(TAG, "%s", "DEBUG");
        Logger.d(TAG, new NullPointerException("d"));
        Logger.d(TAG, new NullPointerException("d"), "DEBUG");

        Logger.i(TAG, "%s", "INFO");
        Logger.i(TAG, new NullPointerException("i"));
        Logger.i(TAG, new NullPointerException("i"), "INFO");

        Logger.w(TAG, "%s", "WARN");
        Logger.w(TAG, new NullPointerException("w"));
        Logger.w(TAG, new NullPointerException("w"), "WARN");

        Logger.e(TAG, "%s", "ERROR");
        Logger.e(TAG, new NullPointerException("e"));
        Logger.e(TAG, new NullPointerException("e"), "ERROR");

        Logger.wtf(TAG, "ASSERT", "ASSERT");
        Logger.wtf(TAG, new NullPointerException("wtf"));
        Logger.wtf(TAG, new NullPointerException("wtf"), "ASSERT");

        Logger.removeLogger(log);

    }

    @Test
    public void cacheTest() {
        CacheLogger cacheLogger = (CacheLogger) Logger.getLogger("CacheLogger");
        cacheLogger.getCacheLogsAndClear();
        Logger.d(TAG, "index1");
        Logger.d(TAG, "index2");
        Logger.d(TAG, "index3");
        Truth.assertThat(cacheLogger.getCacheLogs().size()).isEqualTo(3);
        cacheLogger.getCacheLogsAndClear();
        Truth.assertThat(cacheLogger.getCacheLogs().size()).isEqualTo(0);
    }

    @Test
    public void largeTest() {
        Logger.addLogger(new DebugLogger());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 700; i++) {
            sb.append("cpacm" + i + ",");
        }
        Logger.d(TAG, new NullPointerException("d"), sb.toString());
        Logger.printJson(TAG, "Test Json", "{\"eventId\":\"custom\",\"eventLevelVariable\":{\"grow_index\":\"苹果\",\"grow_click\":14}}");
    }


    @Test
    public void queueTest() {
        CircularFifoQueue<String> queue = new CircularFifoQueue<>(5);
        Truth.assertThat(queue.maxSize()).isEqualTo(5);
        queue.add("value1");
        queue.add("value2");
        queue.offer("value3");
        queue.add("value4");
        queue.add("value5");
        Truth.assertThat(queue.get(2)).isEqualTo("value3");
        try {
            queue.get(10);
        } catch (Exception e) {
            Truth.assertThat(e).isInstanceOf(NoSuchElementException.class);
        }
        queue.poll();
        Truth.assertThat(queue.size()).isEqualTo(4);
        Truth.assertThat(queue.element()).isEqualTo("value2");
        Iterator<String> iterator = queue.iterator();

        while (iterator.hasNext()) {
            Truth.assertThat(iterator.next()).isEqualTo("value2");
            Truth.assertThat(iterator.next()).isEqualTo("value3");
            iterator.remove();
            Truth.assertThat(iterator.next()).isEqualTo("value4");
            iterator.remove();
            Truth.assertThat(iterator.next()).isEqualTo("value5");
            iterator.remove();
        }
        Truth.assertThat(queue.size()).isEqualTo(1);
        queue.clear();
    }


}
