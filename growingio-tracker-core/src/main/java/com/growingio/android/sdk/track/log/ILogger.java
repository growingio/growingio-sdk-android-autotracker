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
package com.growingio.android.sdk.track.log;

public interface ILogger {
    void v(String tag, String message, Object... args);

    void v(String tag, Throwable t, String message, Object... args);

    void v(String tag, Throwable t);

    void d(String tag, String message, Object... args);

    void d(String tag, Throwable t, String message, Object... args);

    void d(String tag, Throwable t);

    void i(String tag, String message, Object... args);

    void i(String tag, Throwable t, String message, Object... args);

    void i(String tag, Throwable t);

    void w(String tag, String message, Object... args);

    void w(String tag, Throwable t, String message, Object... args);

    void w(String tag, Throwable t);

    void e(String tag, String message, Object... args);

    void e(String tag, Throwable t, String message, Object... args);

    void e(String tag, Throwable t);

    void wtf(String tag, String message, Object... args);

    void wtf(String tag, Throwable t, String message, Object... args);

    void wtf(String tag, Throwable t);

    String getType();
}
