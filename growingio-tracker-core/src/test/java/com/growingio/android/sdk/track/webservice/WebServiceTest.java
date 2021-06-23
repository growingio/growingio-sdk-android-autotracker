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

package com.growingio.android.sdk.track.webservice;

import android.app.Activity;
import android.app.Application;
import android.view.MotionEvent;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.RobolectricActivity;
import com.growingio.android.sdk.track.webservices.Circler;
import com.growingio.android.sdk.track.webservices.Debugger;
import com.growingio.android.sdk.track.webservices.DeepLink;
import com.growingio.android.sdk.track.webservices.log.WsLogger;
import com.growingio.android.sdk.track.webservices.message.QuitMessage;
import com.growingio.android.sdk.track.webservices.message.ReadyMessage;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;


@Config()
@RunWith(RobolectricTestRunner.class)
public class WebServiceTest {

    @Test
    public void logTest() {
        WsLogger wsLogger = new WsLogger();
        wsLogger.setCallback(new WsLogger.Callback() {
            @Override
            public void disposeLog(String logMessage) {
                assertThat(logMessage.contains("this is test log"));
            }
        });
        wsLogger.openLog();
        Logger.v("test", "this is test log");
        wsLogger.closeLog();
        wsLogger.printOut();
        wsLogger.setCallback(null);
    }

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void messageTest() {
        JSONObject quit = new QuitMessage().toJSONObject();
        assertThat(quit.opt("msgType")).isEqualTo("quit");

        TrackerContext.init(application);
        JSONObject ready = ReadyMessage.createMessage().toJSONObject();
        assertThat(ready.opt("msgType")).isEqualTo("ready");
    }

    @Test
    public void tipTest() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        TipView tipView = new TipView(activity);
        tipView.setContent("this is test tip");
        tipView.setErrorMessage("this is test error");
        int height = tipView.getStatusBarHeight();

        tipView.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis() + 100L,
                MotionEvent.ACTION_DOWN, 10f, 10f, 1f, 0, 0, 0, 0, 0, 0));
        tipView.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis() + 100L,
                MotionEvent.ACTION_MOVE, 10f, 10f, 1f, 0, 0, 0, 0, 0, 0));
        tipView.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis() + 100L,
                MotionEvent.ACTION_UP, 10f, 10f, 1f, 0, 0, 0, 0, 0, 0));

        tipView.show(activity);
        tipView.dismiss();
    }

    @Test
    public void beanTest() {
        Circler circler = new Circler(new HashMap<>());
        assertThat(circler.getParams().size()).isEqualTo(0);
        Debugger debugger = new Debugger(new HashMap<>());
        assertThat(debugger.getParams().size()).isEqualTo(0);
        DeepLink deepLink = new DeepLink(new HashMap<>());
        deepLink.getParams().put("name", "cpacm");
        assertThat(deepLink.toString()).isEqualTo("name=cpacm\n");
    }

}
