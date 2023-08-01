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
package com.growingio.android.sdk.autotrack;


import android.app.AlertDialog;
import android.app.Application;

import androidx.appcompat.view.menu.ListMenuItemView;
import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.autotrack.shadow.AlertControllerShadow;
import com.growingio.android.sdk.autotrack.shadow.ListMenuItemViewShadow;
import com.growingio.android.sdk.autotrack.util.HurtLocker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
public class UtilTest {
    @Test
    public void hurtLockerTest() {
        try {
            Field field = HurtLocker.getField(RobolectricActivity.class.getName(), "state");
            Truth.assertThat(field.getName()).isEqualTo("state");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Field field = HurtLocker.getField(RobolectricActivity.class, "nosuchfiled");
        } catch (Exception e) {
            Truth.assertThat(e).isInstanceOf(NoSuchFieldException.class);
        }

        try {
            HurtLocker.getInternalState(new RobolectricActivity(), "mTitle");
        } catch (Exception e) {
            Truth.assertThat(e).isInstanceOf(NoSuchFieldException.class);
        }
    }

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void shadowTest() throws Exception {
        AlertControllerShadow acShadow = new AlertControllerShadow(
                new AlertDialog.Builder(application).setTitle("test alert title")
                        .setMessage("test alert message").create());
        Truth.assertThat(acShadow.getTitle().toString()).isEqualTo("test alert title");
        Truth.assertThat(acShadow.getMessage().toString()).isEqualTo("test alert message");


        ListMenuItemView lmiv = new ListMenuItemView(application, null);
        ListMenuItemViewShadow lmivShadow = new ListMenuItemViewShadow(lmiv);
        Truth.assertThat(lmivShadow.getMenuItem()).isNull();
    }

}
