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
import com.growingio.android.sdk.track.utils.ObjectUtils;

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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        sharedPreferences = context.getSharedPreferences(ConstantPool.PREF_FILE_NAME, Context.MODE_PRIVATE);

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
        String abTestKey = ObjectUtils.sha1(deviceId + layerId);
        sharedPreferences.edit().putString(abTestKey, abTestResponse.toSavedJson()).commit();
    }

    @Test
    public void naturalDayABFailedTest() {
        ABTestConfig abTestConfig = new ABTestConfig();
        abTestConfig.setAbTestServerHost("http://localhost:8080");
        context.getConfigurationProvider().addConfiguration(abTestConfig);
        setExpiredABTest("300", System.currentTimeMillis(), System.currentTimeMillis() - 10000L);
        ABTest abTest = new ABTest("300", new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                Truth.assertThat(experiment.getLayerId()).isEqualTo("300");
                Truth.assertThat(experiment.getExperimentId()).isEqualTo(100);
                Truth.assertThat(experiment.getStrategyId()).isEqualTo(100);
                Truth.assertThat(experiment.getVariables().size()).isEqualTo(2);

                Truth.assertThat(dataType).isEqualTo(ABTestCallback.ABTEST_EXPIRED);
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                System.out.println(error.getMessage());
            }
        });
        ABExperiment abExperiment = context.getRegistry().executeData(abTest, ABTest.class, ABExperiment.class);
        Truth.assertThat(abExperiment).isNotNull();
    }
}
