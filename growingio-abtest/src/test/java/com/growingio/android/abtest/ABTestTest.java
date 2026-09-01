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
package com.growingio.android.abtest;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.okhttp3.OkhttpLibraryGioModule;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.abtest.ABExperiment;
import com.growingio.android.sdk.track.middleware.abtest.ABTest;
import com.growingio.android.sdk.track.middleware.abtest.ABTestCallback;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.snappy.XORUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ABTestTest extends MockServer {
    private final Application application = ApplicationProvider.getApplicationContext();
    private TrackerContext context;
    private SharedPreferences sharedPreferences;

    @Before
    public void setup() throws IOException {
        Map<Class<? extends Configurable>, Configurable> sModuleConfigs = new HashMap<>();
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(new CoreConfiguration(ConstantPool.UNKNOWN, "growing.test").setDataSourceId("ABTest"), sModuleConfigs);

        Tracker tracker = new Tracker(application);
        context = tracker.getContext();
        OkhttpLibraryGioModule httpModule = new OkhttpLibraryGioModule();
        httpModule.registerComponents(context);

        sharedPreferences = context.getSharedPreferences(ABTestDataLoader.AB_TEST_PREF_NAME, Context.MODE_PRIVATE);

        ABTestLibraryGioModule module = new ABTestLibraryGioModule();
        ABTestConfig abTestConfig = new ABTestConfig();
        abTestConfig.setAbTestServerHost(MOCK_SERVER_HOST);
        abTestConfig.setAbTestExpired(5, TimeUnit.MINUTES);
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        module.registerComponents(context);

        mockAbTestApiServer();
        start();
    }

    @After
    public void shutdownServer() throws IOException {
        shutdown();
    }

    public void mockAbTestApiServer() {
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String url = request.getRequestUrl().toString();
                URI uri = URI.create(url);
                String expectedPath = "/diversion/specified-layer-variables";
                Truth.assertThat(uri.getPath()).isEqualTo(expectedPath);
                Truth.assertThat(request.getMethod()).ignoringCase().isEqualTo("post");
                String body = request.getBody().readString(StandardCharsets.UTF_8);
                Truth.assertThat(body).contains("accountId=");
                Truth.assertThat(body).contains("datasourceId=");
                Truth.assertThat(body).contains("distinctId=");
                Truth.assertThat(body).contains("layerId=");
                // 未登录时不携带用户标识
                Truth.assertThat(body).doesNotContain("userId=");
                Truth.assertThat(body).doesNotContain("userKey=");
                // stm 恒挂在 query 上
                Truth.assertThat(request.getRequestUrl().queryParameter("stm")).isNotNull();
                return getMockResponse();
            }
        };
        setDispatcher(dispatcher);
    }

    private MockResponse getMockResponse() {
        MockResponse response = new MockResponse();
        response.setResponseCode(200);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("errorMsg", "success");
            jsonObject.put("strategyId", "1");
            jsonObject.put("experimentId", "1");
            JSONObject variables = new JSONObject();
            variables.put("singer", "legend");
            jsonObject.put("variables", variables);
            Buffer buffer = new Buffer();
            buffer.writeUtf8(jsonObject.toString());
            response.setBody(buffer);
        } catch (JSONException ignored) {
        }
        return response;
    }

    @Test
    public void requestABTest() {
        ABTestConfig abTestConfig = new ABTestConfig()
                .setAbTestServerHost(MOCK_SERVER_HOST)
                .setAbTestExpired(5, TimeUnit.MINUTES)
                .setAbTestTimeout(5, TimeUnit.SECONDS);
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        ABTest abTest = new ABTest("100", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(experiment.getLayerId()).isEqualTo("100");
                Truth.assertThat(experiment.getExperimentId()).isEqualTo(1);
                Truth.assertThat(experiment.getStrategyId()).isEqualTo(1);
                Truth.assertThat(experiment.getVariables().size()).isEqualTo(1);

                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_HTTP);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
    }

    @Test
    public void cachedABTest() {
        requestABTest();
        ABTest abTest = new ABTest("100", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(experiment.getLayerId()).isEqualTo("100");
                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_CACHE);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
    }

    @Test
    public void requestABFailedTest() {
        ABTestConfig abTestConfig = new ABTestConfig();
        abTestConfig.setAbTestServerHost("http://localhost:8080");
        context.getConfigurationProvider().addConfiguration(abTestConfig);

        ABTest abTest = new ABTest("100", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {

            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
                Truth.assertThat(error.getMessage()).contains("http://localhost:8080");
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNull();
    }

    @Test
    public void expiredABTest() {
        ABTestConfig abTestConfig = new ABTestConfig()
                .setAbTestServerHost(MOCK_SERVER_HOST);
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        setExpiredABTest("200", System.currentTimeMillis() - 10 * 60 * 1000, ABTestResponse.tomorrowMill());
        ABTest abTest = new ABTest("200", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(experiment.getLayerId()).isEqualTo("200");
                Truth.assertThat(experiment.getExperimentId()).isEqualTo(1);
                Truth.assertThat(experiment.getStrategyId()).isEqualTo(1);
                Truth.assertThat(experiment.getVariables().size()).isEqualTo(1);

                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_HTTP);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
    }

    @Test
    public void naturalDayABTest() {
        ABTestConfig abTestConfig = new ABTestConfig();
        abTestConfig.setAbTestServerHost(MOCK_SERVER_HOST);
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        setExpiredABTest("300", System.currentTimeMillis(), System.currentTimeMillis() - 10000L);
        ABTest abTest = new ABTest("300", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(experiment.getLayerId()).isEqualTo("300");
                Truth.assertThat(experiment.getExperimentId()).isEqualTo(1);
                Truth.assertThat(experiment.getStrategyId()).isEqualTo(1);
                Truth.assertThat(experiment.getVariables().size()).isEqualTo(1);

                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_HTTP);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
    }

    private void setExpiredABTest(String layerId, long expiredTime, long naturalDaytime) {
        ABTestResponse abTestResponse = new ABTestResponse();
        abTestResponse.expiredTime = expiredTime;
        abTestResponse.naturalDaytime = naturalDaytime;
        String deviceId = context.getDeviceInfoProvider().getDeviceId();
        HashMap<String, String> variables = new HashMap<>();
        variables.put("singer", "legend");
        variables.put("game", "haven");
        abTestResponse.abExperiment = new ABExperiment(layerId, 100, 100, variables);
        String userId = context.getUserInfoProvider().getLoginUserId();
        String userKey = context.getUserInfoProvider().getLoginUserKey();
        String abTestKey = ABTestDataLoader.cacheKey(deviceId, userId, userKey, layerId);
        sharedPreferences.edit().putString(abTestKey, abTestResponse.toSavedJson()).commit();
    }

    @Test
    public void requestABTestWithLoginUser() {
        context.getConfigurationProvider().core().setIdMappingEnabled(true);
        context.getUserInfoProvider().setLoginUserId("cpacm", "phone");
        final AtomicBoolean verified = new AtomicBoolean(false);
        setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String stmValue = request.getRequestUrl().queryParameter("stm");
                Truth.assertThat(stmValue).isNotNull();
                long stm = Long.parseLong(stmValue);
                String body = request.getBody().readString(StandardCharsets.UTF_8);
                Map<String, String> params = parseFormBody(body);
                // 服务端视角：用 query 上的 stm & 0xFF 做 Base64 decode → XOR 还原
                Truth.assertThat(deobfuscate(params.get("userId"), stm)).isEqualTo("cpacm");
                Truth.assertThat(deobfuscate(params.get("userKey"), stm)).isEqualTo("phone");
                verified.set(true);
                return getMockResponse();
            }
        });
        ABTest abTest = new ABTest("400", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_HTTP);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
        Truth.assertThat(verified.get()).isTrue();
    }

    @Test
    public void requestABTestWithLoginUserWithoutIdMapping() {
        // idMappingEnabled 默认 false：userKey 被 UserInfoProvider 丢弃，body 只带 userId
        context.getUserInfoProvider().setLoginUserId("cpacm", "phone");
        final AtomicBoolean verified = new AtomicBoolean(false);
        setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String stmValue = request.getRequestUrl().queryParameter("stm");
                String body = request.getBody().readString(StandardCharsets.UTF_8);
                Map<String, String> params = parseFormBody(body);
                Truth.assertThat(deobfuscate(params.get("userId"), Long.parseLong(stmValue))).isEqualTo("cpacm");
                Truth.assertThat(params.containsKey("userKey")).isFalse();
                verified.set(true);
                return getMockResponse();
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(newSimpleABTest("401"), ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
        Truth.assertThat(verified.get()).isTrue();
    }

    @Test
    public void userSwitchCacheTest() {
        final AtomicInteger requestCount = new AtomicInteger(0);
        setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                requestCount.incrementAndGet();
                return getMockResponse();
            }
        });

        // 1. 匿名请求 → HTTP
        Truth.assertThat(context.getRegistry().executeData(newSimpleABTest("500"), ABTest.class, ABExperiment.class)).isNotNull();
        Truth.assertThat(requestCount.get()).isEqualTo(1);

        // 2. 匿名再次请求 → 命中缓存，不发请求
        Truth.assertThat(context.getRegistry().executeData(newSimpleABTest("500"), ABTest.class, ABExperiment.class)).isNotNull();
        Truth.assertThat(requestCount.get()).isEqualTo(1);

        // 3. 登录 userA → 身份进 key，缓存 miss，必须重新请求
        context.getUserInfoProvider().setLoginUserId("userA");
        Truth.assertThat(context.getRegistry().executeData(newSimpleABTest("500"), ABTest.class, ABExperiment.class)).isNotNull();
        Truth.assertThat(requestCount.get()).isEqualTo(2);

        // 4. 登出（A→匿名）→ 命中匿名自己的缓存，各身份记录并存
        context.getUserInfoProvider().setLoginUserId(null);
        Truth.assertThat(context.getRegistry().executeData(newSimpleABTest("500"), ABTest.class, ABExperiment.class)).isNotNull();
        Truth.assertThat(requestCount.get()).isEqualTo(2);
    }

    @Test
    public void cleanExpiredCacheTest() {
        // 有效记录：TTL 内、自然日内
        setExpiredABTest("600", System.currentTimeMillis() + 60_000L, ABTestResponse.tomorrowMill());
        // 过期记录：已过自然日
        setExpiredABTest("601", System.currentTimeMillis(), System.currentTimeMillis() - 10_000L);
        // 脏记录：无法解析
        sharedPreferences.edit().putString("broken", "not a json").commit();
        Truth.assertThat(sharedPreferences.getAll().size()).isEqualTo(3);

        ABTestDataLoader.cleanExpiredCache(sharedPreferences);

        Map<String, ?> all = sharedPreferences.getAll();
        Truth.assertThat(all.size()).isEqualTo(1);
        String deviceId = context.getDeviceInfoProvider().getDeviceId();
        Truth.assertThat(all.containsKey(ABTestDataLoader.cacheKey(deviceId, null, null, "600"))).isTrue();
    }

    private ABTest newSimpleABTest(String layerId) {
        return new ABTest(layerId, new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
    }

    private Map<String, String> parseFormBody(String body) {
        Map<String, String> params = new HashMap<>();
        try {
            for (String pair : body.split("&")) {
                int index = pair.indexOf('=');
                params.put(URLDecoder.decode(pair.substring(0, index), "UTF-8"),
                        URLDecoder.decode(pair.substring(index + 1), "UTF-8"));
            }
        } catch (IOException ignored) {
        }
        return params;
    }

    private String deobfuscate(String encoded, long stm) {
        byte[] data = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP);
        byte[] plain = XORUtils.encrypt(data, (int) (stm & 0xFF));
        return new String(plain, StandardCharsets.UTF_8);
    }

    @Test
    public void naturalDayABFailedTest() {
        // 跨自然日的记录在首次 fetch 时被清理，请求再失败则直接返回失败，无 ABTEST_EXPIRED 兜底
        ABTestConfig abTestConfig = new ABTestConfig();
        abTestConfig.setAbTestServerHost("http://localhost:8080");
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        setExpiredABTest("300", System.currentTimeMillis(), System.currentTimeMillis() - 10000L);
        final AtomicBoolean failed = new AtomicBoolean(false);
        ABTest abTest = new ABTest("300", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                throw new AssertionError("cross-day expired cache should not be returned");
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
                failed.set(true);
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNull();
        Truth.assertThat(failed.get()).isTrue();
        // 跨自然日的记录已被清理
        String deviceId = context.getDeviceInfoProvider().getDeviceId();
        Truth.assertThat(sharedPreferences.contains(ABTestDataLoader.cacheKey(deviceId, null, null, "300"))).isFalse();
    }

    @Test
    public void expiredABFailedTest() {
        // 同日内仅 TTL 过期的记录不被清理，请求失败时返回过期数据兜底（ABTEST_EXPIRED）
        ABTestConfig abTestConfig = new ABTestConfig();
        abTestConfig.setAbTestServerHost("http://localhost:8080");
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        setExpiredABTest("310", System.currentTimeMillis() - 10 * 60 * 1000, ABTestResponse.tomorrowMill());
        ABTest abTest = new ABTest("310", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(experiment.getLayerId()).isEqualTo("310");
                Truth.assertThat(experiment.getExperimentId()).isEqualTo(100);
                Truth.assertThat(experiment.getStrategyId()).isEqualTo(100);
                Truth.assertThat(experiment.getVariables().size()).isEqualTo(2);

                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_EXPIRED);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                throw new AssertionError("TTL-expired cache should be returned as ABTEST_EXPIRED fallback");
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
    }
}
