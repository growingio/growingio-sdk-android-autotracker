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
package com.growingio.android.hybrid;


import android.app.Application;
import android.webkit.WebView;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridViewElementEvent;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.listener.Callback;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE, sdk = 23)
@RunWith(RobolectricTestRunner.class)
public class HybridTest {

    Application application = ApplicationProvider.getApplicationContext();
    private TrackerContext context;

    @Before
    public void setup() {
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(new CoreConfiguration("HybridTest", "growingio://Hybrid"), new HashMap<>());
        Tracker tracker = new Tracker(application);
        tracker.registerComponent(new HybridLibraryGioModule());
        context = tracker.getContext();
    }


    @Test
    public void transformTest() {
        HybridTransformerImp hybridTransformerImp = new HybridTransformerImp();

        String customJson = "{\"eventType\":\"CUSTOM\",\"query\":\"something\",\"domain\":\"growingio.com\",\"path\":\"/webview/button/\",\"eventName\":\"test event\",\"pageShowTimestamp\":12345678,\"attributes\":{\"grow_index\":\"苹果\",\"grow_click\":14}}";
        HybridCustomEvent customEvent = (HybridCustomEvent) hybridTransformerImp.transform(customJson).build();
        Truth.assertThat(customEvent.getQuery()).isEqualTo("something");
        Truth.assertThat(EventBuilderProvider.toJson(customEvent).toString()).contains("test event");
        Truth.assertThat(customEvent.getAttributes().size()).isEqualTo(2);

        String userAttrJson = "{\"eventType\":\"LOGIN_USER_ATTRIBUTES\",\"attributes\":{\"grow_index\":\"苹果\",\"grow_click\":14}}";
        LoginUserAttributesEvent userAttrEvent = (LoginUserAttributesEvent) hybridTransformerImp.transform(userAttrJson).build();
        Truth.assertThat(userAttrEvent.getAttributes().size()).isEqualTo(2);

        String visitAttrJson = "{\"eventType\":\"VISITOR_ATTRIBUTES\",\"attributes\":{\"grow_index\":\"苹果\",\"grow_click\":14}}";
        VisitorAttributesEvent visitAttrEvent = (VisitorAttributesEvent) hybridTransformerImp.transform(visitAttrJson).build();
        Truth.assertThat(visitAttrEvent.getAttributes().size()).isEqualTo(2);

        String converAttrJson = "{\"eventType\":\"CONVERSION_VARIABLES\",\"attributes\":{\"grow_index\":\"苹果\",\"grow_click\":14}}";
        ConversionVariablesEvent converAttrEvent = (ConversionVariablesEvent) hybridTransformerImp.transform(converAttrJson).build();
        Truth.assertThat(converAttrEvent.getAttributes().size()).isEqualTo(2);

        String formJson = "{\"eventType\":\"FORM_SUBMIT\",\"hyperlink\":\"www.growingio.com\",\"domain\":\"growingio.com\",\"query\":\"something\",\"index\":1,\"textValue\":\"test\",\"xpath\":\"/webview/user/cpacm\",\"path\":\"urlscheme\",\"pageShowTimestamp\":1234567890}";
        HybridViewElementEvent postEvent = (HybridViewElementEvent) hybridTransformerImp.transform(formJson).build();
        Truth.assertThat(postEvent.getQuery()).isEqualTo("something");
        Truth.assertThat(postEvent.getTextValue()).isEqualTo("test");

        String viewChangeJson = "{\"eventType\":\"VIEW_CHANGE\",\"hyperlink\":\"www.growingio.com\",\"domain\":\"growingio.com\",\"query\":\"something\",\"index\":1,\"textValue\":\"test\",\"xpath\":\"/webview/user/cpacm\",\"path\":\"urlscheme\",\"pageShowTimestamp\":1234567890}";
        HybridViewElementEvent viewChangeEvent = (HybridViewElementEvent) hybridTransformerImp.transform(viewChangeJson).build();
        Truth.assertThat(viewChangeEvent.getHyperlink()).isEqualTo("www.growingio.com");
        Truth.assertThat(viewChangeEvent.getXpath()).isEqualTo("/webview/user/cpacm");

        String viewClickJson = "{\"eventType\":\"VIEW_CLICK\",\"hyperlink\":\"www.growingio.com\",\"domain\":\"growingio.com\",\"query\":\"something\",\"index\":1,\"textValue\":\"test\",\"xpath\":\"/webview/user/cpacm\",\"path\":\"urlscheme\",\"pageShowTimestamp\":1234567890}";
        HybridViewElementEvent viewClickEvent = (HybridViewElementEvent) hybridTransformerImp.transform(viewClickJson).build();
        Truth.assertThat(viewClickEvent.getIndex()).isEqualTo(1);
        Truth.assertThat(viewClickEvent.getPath()).isEqualTo("urlscheme");

        String pageJson = "{\"eventType\":\"PAGE\",\"domain\":\"growingio.com\",\"protocolType\":\"webview\",\"query\":\"something\",\"index\":1,\"textValue\":\"test\",\"path\":\"urlscheme\",\"referralPage\":\"/home/product/\",\"timestamp\":123456789,\"title\":\"this is a test page.\"}";
        HybridPageEvent pageEvent = (HybridPageEvent) hybridTransformerImp.transform(pageJson).build();
        Truth.assertThat(pageEvent.getProtocolType()).isEqualTo("webview");
        Truth.assertThat(pageEvent.getQuery()).isEqualTo("something");
    }

    @Test
    public void providerTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().get();
        WebView webView = new WebView(activity);
        SuperWebView<WebView> superWebView = SuperWebView.make(webView);
        OnDomChangedListener testListener = () -> {
        };
        HybridBridgeProvider hybridBridgeProvider = context.getProvider(HybridBridgeProvider.class);

        hybridBridgeProvider.registerDomChangedListener(testListener);
        hybridBridgeProvider.unregisterDomChangedListener(testListener);
        hybridBridgeProvider.bridgeForWebView(superWebView);
        hybridBridgeProvider.getWebViewDomTree(superWebView, new Callback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                System.out.println(result);
                Truth.assertThat(result).isNotNull();
            }

            @Override
            public void onFailed() {
                System.out.println("onFailed");
            }
        });
    }

    @Test
    public void bridgeInterfaceTest() {
        HybridBridgeProvider hybridBridgeProvider = context.getProvider(HybridBridgeProvider.class);
        UserInfoProvider userInfoProvider = context.getUserInfoProvider();
        ConfigurationProvider configurationProvider = context.getConfigurationProvider();

        WebViewJavascriptBridgeConfiguration configuration = new WebViewJavascriptBridgeConfiguration("test", "test", "test", "test", "test", "test", 23);
        WebViewBridgeJavascriptInterface webInterface = new WebViewBridgeJavascriptInterface(configuration, hybridBridgeProvider, userInfoProvider);
        webInterface.setNativeUserId("cpacm");
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Truth.assertThat(userInfoProvider.getLoginUserId()).isEqualTo("cpacm");
        webInterface.clearNativeUserId();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Truth.assertThat(userInfoProvider.getLoginUserId()).isEqualTo("");

        configurationProvider.core().setIdMappingEnabled(true);
        webInterface.setNativeUserIdAndUserKey("cpacm", "email");
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Truth.assertThat(userInfoProvider.getLoginUserId()).isEqualTo("cpacm");
        Truth.assertThat(userInfoProvider.getLoginUserKey()).isEqualTo("email");

        webInterface.clearNativeUserIdAndUserKey();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Truth.assertThat(userInfoProvider.getLoginUserId()).isEqualTo("");
        Truth.assertThat(userInfoProvider.getLoginUserKey()).isEqualTo("");
        configurationProvider.core().setIdMappingEnabled(false);

        String testJson = "{\"eventType\":\"LOGIN_USER_ATTRIBUTES\",\"attributes\":{\"grow_index\":\"苹果\",\"grow_click\":14}}";
        webInterface.dispatchEvent(testJson);
        Truth.assertThat(webInterface.getConfiguration()).contains("23");
        webInterface.onDomChanged();

        Robolectric.flushForegroundThreadScheduler();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

}
