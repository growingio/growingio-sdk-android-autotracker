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

package com.growingio.sdk.sample;

import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2021/6/30
 */
public class TestActionProvider {

    public static void viewOnClick(int obj) {
        System.out.println(obj);
    }

    public static void alertDialogShow(Object obj) {
        System.out.println(obj);
    }

    public static void menuItemOnClick(TestOnClickListener listener, Object obj) {
        System.out.println(obj);
    }

    public static void bridgeForWebView(TestOnClickListener listener, String url) {
        System.out.println(url);
    }

    public static void createOrResumePage(TestOnClickListener listener) {
        System.out.println(listener);
    }

    public static String types(float type1, double type2, char type3, short type4, boolean type5, byte type6) {
        return "";
    }

    public static void arrays(List<String> params) {
    }


}
