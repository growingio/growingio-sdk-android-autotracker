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
package com.growingio.android.sdk.track.view;

import android.app.Activity;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.growingio.android.sdk.track.providers.RobolectricActivity;
import com.growingio.android.sdk.track.middleware.webservice.Circler;
import com.growingio.android.sdk.track.middleware.webservice.Debugger;
import com.growingio.android.sdk.track.middleware.advert.DeepLink;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;


@Config()
@RunWith(RobolectricTestRunner.class)
public class TipViewTest {

    @Test
    public void tipTest() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();

        TipView tipView = new TipView(activity);
        WindowManager windowManager = activity.getWindowManager();
        windowManager.addView(tipView, new WindowManager.LayoutParams());
        tipView.setContent("this is test tip");
        tipView.setErrorMessage("this is test error");

        tipView.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis() + 100L,
                MotionEvent.ACTION_DOWN, 10f, 10f, 1f, 0, 0, 0, 0, 0, 0));
        tipView.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis() + 100L,
                MotionEvent.ACTION_MOVE, 100f, 100f, 1f, 0, 0, 0, 0, 0, 0));
        tipView.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis() + 100L,
                MotionEvent.ACTION_UP, 10f, 10f, 1f, 0, 0, 0, 0, 0, 0));

        tipView.ready(activity);
        tipView.show(activity);
        tipView.dismiss();
    }

    @Test
    public void beanTest() {
        Circler circler = new Circler(new HashMap<>());
        assertThat(circler.getParams().size()).isEqualTo(0);
        Debugger debugger = new Debugger(new HashMap<>());
        assertThat(debugger.getParams().size()).isEqualTo(0);
        DeepLink deepLink = new DeepLink(Uri.parse("growingio://cpacm?name=cpacm"));
        assertThat(deepLink.getUri().toString()).contains("name=cpacm");
    }

}
