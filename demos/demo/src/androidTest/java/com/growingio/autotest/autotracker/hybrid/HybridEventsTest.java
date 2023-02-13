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

package com.growingio.autotest.autotracker.hybrid;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.WebViewActivity;
import com.google.common.truth.Truth;
import com.growingio.android.hybrid.HybridBridgeProvider;
import com.growingio.android.hybrid.OnDomChangedListener;
import com.growingio.android.hybrid.SuperWebView;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.utils.JsonUtil;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;
import com.growingio.autotest.help.TrackHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HybridEventsTest extends EventsTest {
    private static final String TAG = "HybridEventsTest";

    private static final HashMap<String, String> TEST_ATTRIBUTES = new HashMap<String, String>() {{
        put("key1", "value1");
        put("key2", "value2");
        put("key3", "");
        put("key4", null);
    }};

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig()
        .setIdMappingEnabled(true));
    }

    @Override
    @Before
    public void setUp() throws IOException {
        super.setUp();
        getEventsApiServer().setCheckDomain(false);
        getEventsApiServer().setCheckTimestamp(false);
    }

    private WebView launchMockWebView() {
        WebViewActivity.setLoadUrl("file:///android_asset/autotrackHybridTest.html");
        ActivityScenario<WebViewActivity> launch = ActivityScenario.launch(WebViewActivity.class);
        AtomicReference<WebViewActivity> webViewActivity = new AtomicReference<>();
        launch.onActivity(webViewActivity::set);
        //等待WebView显示正常
        onWebView().withElement(findElement(Locator.ID, "mark")).check(webMatches(getText(), containsString("hybrid")));
        return webViewActivity.get().getWebView();
    }

    @Test
    public void webViewJavascriptBridgeConfigurationTest() {
        AtomicBoolean receivedConfig = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:getWebViewJavascriptBridgeConfiguration()", value -> {
                    Log.e(TAG, "webViewJavascriptBridgeConfigurationTest: " + value);
                    try {
                        JSONObject config = new JSONObject(value);
                        Truth.assertThat(config.getString("appId")).isEqualTo("testUrlScheme");
                        Truth.assertThat(config.getString("appPackage")).isEqualTo("com.gio.test.three");
                        Truth.assertThat(config.getString("projectId")).isEqualTo("testProjectId");
                        Truth.assertThat(config.getString("nativeSdkVersion")).isEqualTo(SDKConfig.SDK_VERSION);
                        Truth.assertThat(config.getInt("nativeSdkVersionCode")).isEqualTo(SDKConfig.SDK_VERSION_CODE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Truth.assertWithMessage(e.getMessage()).fail();
                    }
                    receivedConfig.set(true);
                }));
        Awaiter.untilTrue(receivedConfig);
    }

    @Test
    public void hybridCustomEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("eventName").equals("test_name")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("path")).isEqualTo("/push/web.html");
                        Truth.assertThat(jsonObject.getLong("pageShowTimestamp")).isEqualTo(1602485626878L);
                        Truth.assertThat(jsonObject.getString("query")).isEqualTo("a=1&b=2");
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockCustomEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridCustomEventWithAttributesTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("eventName").equals("test_name")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("path")).isEqualTo("/push/web.html");
                        Truth.assertThat(jsonObject.getLong("pageShowTimestamp")).isEqualTo(1602485626878L);
                        Truth.assertThat(jsonObject.getString("query")).isEqualTo("a=1&b=2");
                        HashMap<String, String> attributes = (HashMap<String, String>) JsonUtil.copyToMap(jsonObject.getJSONObject("attributes"));
                        Truth.assertThat(attributes).isEqualTo(TEST_ATTRIBUTES);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockCustomEventWithAttributes()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridVisitorAttributesEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitorAttributesEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    HashMap<String, String> attributes = (HashMap<String, String>) JsonUtil.copyToMap(jsonObject.getJSONObject("attributes"));
                    if (attributes.equals(TEST_ATTRIBUTES)) {
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockVisitorAttributesEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridLoginUserAttributesEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedLoginUserAttributesEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    HashMap<String, String> attributes = (HashMap<String, String>) JsonUtil.copyToMap(jsonObject.getJSONObject("attributes"));
                    if (attributes.equals(TEST_ATTRIBUTES)) {
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockLoginUserAttributesEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridConversionVariablesEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedConversionVariablesEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    HashMap<String, String> attributes = (HashMap<String, String>) JsonUtil.copyToMap(jsonObject.getJSONObject("attributes"));
                    if (attributes.equals(TEST_ATTRIBUTES)) {
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockConversionVariablesEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridPageEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("title")).isEqualTo("Hybrid测试页面");
                        Truth.assertThat(jsonObject.getString("referralPage")).isEqualTo("http://test-browser.growingio.com/push");
                        Truth.assertThat(jsonObject.getString("protocolType")).isEqualTo("https");
                        Truth.assertThat(jsonObject.getLong("timestamp")).isEqualTo(1602485628504L);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockPageEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridPageEventWithAttributesTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("title")).isEqualTo("Hybrid测试页面");
                        Truth.assertThat(jsonObject.getString("referralPage")).isEqualTo("http://test-browser.growingio.com/push");
                        Truth.assertThat(jsonObject.getString("protocolType")).isEqualTo("https");
                        Truth.assertThat(jsonObject.getLong("timestamp")).isEqualTo(1602485628504L);
                        HashMap<String, String> attributes = (HashMap<String, String>) JsonUtil.copyToMap(jsonObject.getJSONObject("attributes"));
                        if (attributes.equals(TEST_ATTRIBUTES)) {
                            receivedEvent.set(true);
                        }
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockPageWithAttributesEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridPageEventWithQueryTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("title")).isEqualTo("Hybrid测试页面");
                        Truth.assertThat(jsonObject.getString("query")).isEqualTo("a=1&b=2");
                        Truth.assertThat(jsonObject.getString("referralPage")).isEqualTo("http://test-browser.growingio.com/push");
                        Truth.assertThat(jsonObject.getString("protocolType")).isEqualTo("https");
                        Truth.assertThat(jsonObject.getLong("timestamp")).isEqualTo(1602485628504L);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockPageEventWithQuery()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridFilePageEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("com.gio.test.three");
                        Truth.assertThat(jsonObject.getString("title")).isEqualTo("Hybrid测试页面");
                        Truth.assertThat(jsonObject.getString("protocolType")).isEqualTo("file");
                        Truth.assertThat(jsonObject.getLong("timestamp")).isEqualTo(1602485628504L);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockFilePageEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridViewClickEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedViewClickEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("textValue")).isEqualTo("登录");
                        Truth.assertThat(jsonObject.getString("xpath")).isEqualTo("/div/button#abc");
                        Truth.assertThat(jsonObject.getString("hyperlink")).isEqualTo("https://www.growingio.com");
                        Truth.assertThat(jsonObject.getInt("index")).isEqualTo(1);
                        Truth.assertThat(jsonObject.getLong("pageShowTimestamp")).isEqualTo(1602485626878L);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockViewClickEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridViewChangeEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedViewChangeEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("textValue")).isEqualTo("输入内容");
                        Truth.assertThat(jsonObject.getString("xpath")).isEqualTo("/div/form/input");
                        Truth.assertThat(jsonObject.optString("hyperlink", "")).isEqualTo("");
                        Truth.assertThat(jsonObject.getInt("index")).isEqualTo(1);
                        Truth.assertThat(jsonObject.getLong("pageShowTimestamp")).isEqualTo(1602485626878L);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockViewChangeEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridFormSubmitEventTest() {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedHybridFormSubmitEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("path").equals("/push/web.html")) {
                        Truth.assertThat(jsonObject.getString("domain")).isEqualTo("test-browser.growingio.com");
                        Truth.assertThat(jsonObject.getString("xpath")).isEqualTo("/div/form/input");
                        Truth.assertThat(jsonObject.optString("hyperlink", "")).isEqualTo("");
                        Truth.assertThat(jsonObject.optString("textValue", "")).isEqualTo("");
                        Truth.assertThat(jsonObject.getInt("index")).isEqualTo(1);
                        Truth.assertThat(jsonObject.getLong("pageShowTimestamp")).isEqualTo(1602485626878L);
                        receivedEvent.set(true);
                    }
                }
            }
        });

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:sendMockFormSubmitEvent()", null));
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void hybridUserIdChangeTest() {
        getEventsApiServer().setCheckUserId(false);
        getEventsApiServer().setCheckSessionId(false);
        WebView webView = launchMockWebView();
        String loginUserId = PersistentDataProvider.get().getLoginUserId();
        String newUserId = loginUserId + "hybrid";
        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:setUserId(\"" + newUserId + "\")", null));
        Awaiter.untilTrue(() -> newUserId.equals(PersistentDataProvider.get().getLoginUserId()));

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:clearUserId()", null));
        Awaiter.untilTrue(() -> TextUtils.isEmpty(PersistentDataProvider.get().getLoginUserId()));

        String loginUserKey = PersistentDataProvider.get().getLoginUserKey();
        String newUserKey = loginUserKey + "hybridUserKey";
        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:setUserIdAndUserKey(\"" + newUserId + "\" , \"" + newUserKey  + "\")", null));
        Awaiter.untilTrue(() -> newUserId.equals(PersistentDataProvider.get().getLoginUserId()) &&
                newUserKey.equals(PersistentDataProvider.get().getLoginUserKey()));

        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:clearUserIdAndUserKey()", null));
        Awaiter.untilTrue(() -> TextUtils.isEmpty(PersistentDataProvider.get().getLoginUserId()) &&
                TextUtils.isEmpty(PersistentDataProvider.get().getLoginUserKey()));
    }

    @Test
    public void hybridDomChangedTest() throws Exception {
        AtomicBoolean receivedEvent = new AtomicBoolean(false);
        WebView webView = launchMockWebView();
        SuperWebView<WebView> superWebView = (SuperWebView<WebView>) Whitebox.invokeConstructor(Class.forName("com.growingio.android.hybrid.SuperWebView$SystemWebView"), webView);
        HybridBridgeProvider.get().registerDomChangedListener(new OnDomChangedListener() {
            @Override
            public void onDomChanged() {
                TrackHelper.postToUiThread(() ->
                        HybridBridgeProvider.get().getWebViewDomTree(superWebView, new Callback<JSONObject>() {
                            @Override
                            public void onSuccess(JSONObject result) {
                                receivedEvent.set(result != null);
                            }

                            @Override
                            public void onFailed() {

                            }
                        }));
            }
        });
        TrackHelper.postToUiThread(() ->
                webView.evaluateJavascript("javascript:mockDomChanged()", null));

        Awaiter.untilTrue(receivedEvent);
    }
}
