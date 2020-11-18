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

package com.growingio.android.sdk.autotrack.inject;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AliasActivity;
import android.app.ExpandableListActivity;
import android.app.LauncherActivity;
import android.app.ListActivity;
import android.app.NativeActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.preference.PreferenceActivity;

import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.sdk.inject.annotation.BeforeSuper;

public class ActivityInjector {
    private ActivityInjector() {
    }

    @BeforeSuper(clazz = Activity.class,                     method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = AccountAuthenticatorActivity.class, method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = ActivityGroup.class,                method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = AliasActivity.class,                method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = ExpandableListActivity.class,       method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = LauncherActivity.class,             method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = ListActivity.class,                 method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = NativeActivity.class,               method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = TabActivity.class,                  method = "onNewIntent", parameterTypes = {Intent.class})
    @BeforeSuper(clazz = PreferenceActivity.class,           method = "onNewIntent", parameterTypes = {Intent.class})
    public static void onActivityNewIntent(Activity activity, Intent intent) {
        ActivityStateProvider.get().onActivityNewIntent(activity, intent);
    }
}
