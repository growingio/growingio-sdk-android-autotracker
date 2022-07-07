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

package com.growingio.android.analytics.google;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.analytics.Tracker;
import com.google.common.truth.Truth;
import com.growingio.android.analytics.google.model.AnalyticsEvent;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.any;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 30)
@PowerMockIgnore({
        "org.mockito.*",
        "org.robolectric.*",
        "androidx.*",
        "android.*",
        "org.json.*"
})
@PrepareOnlyThisForTest({GoogleAnalyticsAdapter.class})
public class GoogleAnalyticsAdapterTest {
    GoogleAnalyticsAdapter spyAdapter;
    Application spyApplication;
    InputStream inputStream;

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    @Before
    public void setup() throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        inputStream = getClass().getClassLoader().getResourceAsStream("res/xml/global_tracker.xml");
        xpp.setInput(inputStream, "UTF-8");

        XmlResourceParser xmlResourceParser = mock(XmlResourceParser.class);
        when(xmlResourceParser.getName()).thenAnswer(invocation -> xpp.getName());
        when(xmlResourceParser.getAttributeValue(any(), any())).thenAnswer(invocation -> xpp.getAttributeValue(null, "name"));
        when(xmlResourceParser.getEventType()).thenAnswer(invocation -> xpp.getEventType());
        when(xmlResourceParser.next()).thenAnswer(invocation -> xpp.next());
        when(xmlResourceParser.getText()).thenAnswer(invocation -> xpp.getText());
        doAnswer(invocation -> {
            inputStream.close();
            inputStream = getClass().getClassLoader().getResourceAsStream("res/xml/global_tracker.xml");
            xpp.setInput(inputStream, "UTF-8");
            return null;
        }).when(xmlResourceParser).close();

        Resources mockResources = mock(Resources.class);
        when(mockResources.getXml(anyInt())).thenReturn(xmlResourceParser);

        spyApplication = spy(ApplicationProvider.getApplicationContext());
        when(spyApplication.getResources()).thenReturn(mockResources);

        TrackerContext.init(spyApplication);
        TrackerContext.initSuccess();
        HashMap<String, String> dataSourceIds = new HashMap<>();
        dataSourceIds.put("UA-000000000-1", "0000000000000001");
        dataSourceIds.put("UA-000000000-2", "0000000000000002");
        HashMap<Class<? extends Configurable>, Configurable> moduleConfigs = new HashMap<>();
        moduleConfigs.put(GoogleAnalyticsConfiguration.class, new GoogleAnalyticsConfiguration().setDatasourceIds(dataSourceIds));
        ConfigurationProvider.initWithConfig(new CoreConfiguration("FAKE_PROJECT_ID", "FAKE_URL_SCHEME"), moduleConfigs);
        spyAdapter = spy(GoogleAnalyticsAdapter.get());
    }

    @Test
    public void newTrackerTest() throws Exception {
        String clientId = "1000000000000001";
        Tracker tracker = mock(Tracker.class);
        when(tracker.get("&cid")).thenReturn(clientId);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
            Object event = invocation.getArguments()[0];
            AnalyticsEvent analyticsEvent = (AnalyticsEvent) event;
            JSONObject eventJson = analyticsEvent.toJSONObject();
            if (analyticsEvent.getEventType().equals(TrackEventType.LOGIN_USER_ATTRIBUTES)) {
                Truth.assertThat(eventJson.getString("dataSourceId")).isEqualTo("0000000000000001");
                JSONObject attributes = eventJson.optJSONObject("attributes");
                Truth.assertThat(attributes).isNotNull();
                Truth.assertThat(attributes.getString("&cid")).isEqualTo("1000000000000001");
                countDownLatch.countDown();
            }
            return invocation.callRealMethod();
        }).when(spyAdapter, "postAnalyticsEvent", any(AnalyticsEvent.class));

        spyAdapter.newTracker(tracker, 0x00001);
        countDownLatch.await(3000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void sendTest() throws Exception {
        String measurementId = "UA-000000000-2";
        Tracker tracker = mock(Tracker.class);
        when(tracker.get("&tid")).thenReturn(measurementId);
        spyAdapter.newTracker(tracker, measurementId);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Object event = invocation.getArguments()[0];
            AnalyticsEvent analyticsEvent = (AnalyticsEvent) event;
            JSONObject eventJson = analyticsEvent.toJSONObject();
            if (analyticsEvent.getEventType().equals(TrackEventType.CUSTOM)) {
                Truth.assertThat(eventJson.getString("dataSourceId")).isEqualTo("0000000000000002");
                JSONObject attributes = eventJson.optJSONObject("attributes");
                Truth.assertThat(attributes).isNotNull();
                Truth.assertThat(attributes.getString("&ea")).isEqualTo("click");
                Truth.assertThat(attributes.getString("&t")).isEqualTo("event");
                countDownLatch.countDown();
            }
            return invocation.callRealMethod();
        }).when(spyAdapter, "postAnalyticsEvent", any(AnalyticsEvent.class));

        HashMap<String, String> params = new HashMap<>();
        params.put("&ea", "click");
        params.put("&t", "event");
        spyAdapter.send(tracker, params);

        countDownLatch.await(3000, TimeUnit.MILLISECONDS);
    }
}
