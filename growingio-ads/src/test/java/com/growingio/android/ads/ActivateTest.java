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
package com.growingio.android.ads;

import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.middleware.ads.Activate;
import com.growingio.android.sdk.track.middleware.ads.AdsResult;
import com.growingio.android.sdk.track.middleware.ads.DeepLinkCallback;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;
import com.growingio.android.sdk.track.utils.ConstantPool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;

import java.util.HashMap;
import java.util.Map;

/**
 * need java 9+
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ActivateTest {
    private final Application application = ApplicationProvider.getApplicationContext();
    private TrackerContext context;

    @Before
    public void setup() {
        Map<Class<? extends Configurable>, Configurable> sModuleConfigs = new HashMap<>();
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(new CoreConfiguration(ConstantPool.UNKNOWN, "growing.test"), sModuleConfigs);

        Tracker tracker = new Tracker(application);
        context = tracker.getContext();

        Robolectric.buildActivity(RobolectricActivity.class).setup().create().get();
    }


    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void activate() {
        AdsLibraryGioModule module = new AdsLibraryGioModule();
        module.registerComponents(context);

        AdsUtils.clearAdsSharedPreferences();
        Truth.assertThat(AdsUtils.isDeviceActivated()).isFalse();

        context.getRegistry().executeData(Activate.activate(), Activate.class, AdsResult.class);
        // 等待 view.post 完成
        shadowOf(Looper.getMainLooper()).idle();
        Truth.assertThat(AdsUtils.isDeviceActivated()).isTrue();
    }


    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void deeplinkUri() {
        AdsConfig config = new AdsConfig();
        config.setDeepLinkCallback((params, error, appAwakePassedTime) -> {
            Truth.assertThat(error).isEqualTo(0);
            Truth.assertThat(params.toString()).isEqualTo("{name=cpacm}");
        });
        context.getConfigurationProvider().addConfiguration(config);
        AdsLibraryGioModule module = new AdsLibraryGioModule();
        module.registerComponents(context);
        Uri uri = Uri.parse("growing.test://customPath?deep_link_id=1111&deep_click_id=2222&deep_click_time=3333&deep_params={\"name\":\"cpacm\"}");
        context.getRegistry().executeData(Activate.deeplink(uri), Activate.class, AdsResult.class);
        shadowOf(Looper.getMainLooper()).idle();
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void clipBoardTest() {
        AdsConfig config = new AdsConfig();
        config.setDeepLinkCallback((params, error, appAwakePassedTime) -> {
            Truth.assertThat(params.toString()).isEqualTo("{name=cpacm}");
        });
        context.getConfigurationProvider().addConfiguration(config);
        AdsLibraryGioModule module = new AdsLibraryGioModule();
        module.registerComponents(context);

        String clipData = "{\n" +
                "  \"type\":\"gads\", \n" +
                "  \"deep_link_id\":\"xxxxx\",\n" +
                "  \"deep_click_id\":\"xxxx\",\n" +
                "  \"deep_click_time\":\"\",\n" +
                "  \"deep_params\": \"{name:cpacm}\",\n" +
                "  \"scheme\":\"growing.test\"\n" +
                "}";

        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText(null, convertToZws(clipData)));
        context.getRegistry().executeData(Activate.activate(), Activate.class, AdsResult.class);
        shadowOf(Looper.getMainLooper()).idle();
    }

    private String convertToZws(String data) {
        StringBuilder binary = new StringBuilder();
        StringBuilder result = new StringBuilder();
        char[] chars = data.toCharArray();
        for (char aChar : chars) {
            StringBuilder hexB = new StringBuilder(Integer.toBinaryString(aChar));
            while (hexB.length() < 16) {
                hexB.insert(0, "0");
            }
            binary.append(hexB);
        }

        char[] chars2 = binary.toString().toCharArray();
        for (char bChar : chars2) {
            if (bChar == 48) { // (char)0
                result.append((char) 8204);
            } else {
                result.append((char) 8205);
            }
        }
        return result.toString();
    }

    @Test
    public void zwsConvertTest() {
        String data = "cpacm";
        String content = convertToZws(data);
        Truth.assertThat(AdsUtils.parseZwsData(content)).isEqualTo(data);
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    @Config(shadows = {ShadowDeviceInfoProvider.class, ShadowThreadUtils.class})
    public void shortUrlTest() {
        AdsConfig config = new AdsConfig();
        config.setDeepLinkHost("https://www.google.com");
        context.getConfigurationProvider().addConfiguration(config);
        AdsLibraryGioModule module = new AdsLibraryGioModule();
        module.registerComponents(context);

        AdsUtils.clearAdsSharedPreferences();

        Uri uri = Uri.parse("https://www.google.com/eujsasjf");
        context.getRegistry().executeData(Activate.handleDeeplink(uri, new DeepLinkCallback() {
            @Override
            public void onReceive(Map<String, String> params, int error, long appAwakePassedTime) {
                Truth.assertThat(error).isEqualTo(DeepLinkCallback.ERROR_NET_FAIL);
            }
        }), Activate.class, AdsResult.class);
        shadowOf(Looper.getMainLooper()).idle();
    }

    @Implements(DeviceInfoProvider.class)
    public static class ShadowDeviceInfoProvider {
        @Implementation
        public String getUserAgent() {
            return "cpacm";
        }
    }

    @Implements(TrackMainThread.class)
    public static class ShadowThreadUtils {
        @Implementation
        public static void runOnUiThread(Runnable r) {
            r.run();
        }
    }

}
