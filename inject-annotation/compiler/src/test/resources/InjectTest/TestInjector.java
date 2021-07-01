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

package com.growingio.sdk.test;

import java.util.List;
import com.growingio.sdk.inject.annotation.AfterSuper;
import com.growingio.sdk.inject.annotation.Before;
import com.growingio.sdk.sample.TestOnClickListener;
import com.growingio.sdk.sample.TestActionProvider;
import com.growingio.sdk.inject.annotation.After;
import com.growingio.sdk.inject.annotation.BeforeSuper;

public class TestInjector {
    private static final String TAG = "ViewClickInjector";

    @BeforeSuper(clazz = TestOnClickListener.class, method = "onClick", parameterTypes = {int.class})
    public static void viewOnClick(TestOnClickListener listener, int view) {
        TestActionProvider.viewOnClick(view);
    }

    @Before(clazz = TestOnClickListener.class, method = "loadUrl", parameterTypes = {String.class})
    public static void webkitWebViewLoadUrl(TestOnClickListener webView, String url) {
        TestActionProvider.bridgeForWebView(webView, url);
    }

    @After(clazz = TestOnClickListener.class, method = "show")
    public static void alertDialogShow(Object alertDialog) {
        TestActionProvider.alertDialogShow(alertDialog);
    }

    @BeforeSuper(clazz = TestOnClickListener.class, method = "onOptionsItemSelected", parameterTypes = {Object.class}, returnType = boolean.class)
    @BeforeSuper(clazz = TestOnClickListener.class, method = "onOptionsItemSelected2", parameterTypes = {Object.class}, returnType = boolean.class)
    public static void menuItemOnOptionsItemSelected(TestOnClickListener listener, Object item) {
        TestActionProvider.menuItemOnClick(listener, item);
    }

    @AfterSuper(clazz = TestOnClickListener.class, method = "onResume")
    @AfterSuper(clazz = TestOnClickListener.class, method = "onResume2")
    public static void systemFragmentOnResume(TestOnClickListener listener) {
        TestActionProvider.createOrResumePage(listener);
    }


    @After(clazz = TestOnClickListener.class, method = "types", parameterTypes = {float.class, double.class, char.class, short.class, boolean.class, byte.class}, returnType = String.class)
    public static void types(TestOnClickListener webView, float type1, double type2, char type3, short type4, boolean type5, byte type6) {
        TestActionProvider.types(type1, type2, type3, type4, type5, type6);
    }

    @After(clazz = TestOnClickListener.class, method = "arrays", parameterTypes = {List.class}, returnType = Object.class)
    public static void arrays(TestOnClickListener webView, List<String> params) {
        TestActionProvider.arrays(params);
    }
}

