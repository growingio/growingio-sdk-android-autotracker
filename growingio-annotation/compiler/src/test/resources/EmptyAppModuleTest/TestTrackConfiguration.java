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

package com.growingio.android.sdk.test;

import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.sdk.annotation.compiler.sample.EmptyConfig;
import java.util.HashMap;

/**
 * The entry point for interacting with GrowingIO Tracker for Applications
 * <p>Includes all generated configs from Tracker in source.
 *
 * <p>This class is generated and should not be modified
 */
public final class TestTrackConfiguration {
    private final CoreConfiguration coreConfiguration;

    private final HashMap<Class<? extends Configurable>, Configurable> MODULE_CONFIGURATIONS = new HashMap<Class<? extends Configurable>, Configurable>();

    public TestTrackConfiguration(String projectId, String urlScheme) {
        this.coreConfiguration = new CoreConfiguration(projectId,urlScheme);
        addConfiguration(new EmptyConfig());
    }

    public CoreConfiguration core() {
        return coreConfiguration;
    }

    public HashMap<Class<? extends Configurable>, Configurable> getConfigModules() {
        return MODULE_CONFIGURATIONS;
    }

    public void addConfiguration(Configurable config) {
        if (config != null) {
            MODULE_CONFIGURATIONS.put(config.getClass(), config);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfiguration(Class<T> clazz) {
        return (T) MODULE_CONFIGURATIONS.get(clazz);
    }

    public final TestTrackConfiguration setProject(String projectId, String urlScheme) {
        core().setProject(projectId, urlScheme);
        return this;
    }

    public final String getProjectId() {
        return core().getProjectId();
    }

    public final boolean isDataCollectionEnabled() {
        return core().isDataCollectionEnabled();
    }

    public final TestTrackConfiguration setDataCollectionEnabled(boolean dataCollectionEnabled) {
        core().setDataCollectionEnabled(dataCollectionEnabled);
        return this;
    }

    public final String getUrlScheme() {
        return core().getUrlScheme();
    }

    public final String getChannel() {
        return core().getChannel();
    }

    public final TestTrackConfiguration setChannel(String channel) {
        core().setChannel(channel);
        return this;
    }

    public final boolean isUploadExceptionEnabled() {
        return core().isUploadExceptionEnabled();
    }

    public final TestTrackConfiguration setUploadExceptionEnabled(boolean uploadExceptionEnabled) {
        core().setUploadExceptionEnabled(uploadExceptionEnabled);
        return this;
    }

    public final boolean isDebugEnabled() {
        return core().isDebugEnabled();
    }

    public final TestTrackConfiguration setDebugEnabled(boolean enabled) {
        core().setDebugEnabled(enabled);
        return this;
    }

    public final int getCellularDataLimit() {
        return core().getCellularDataLimit();
    }

    public final TestTrackConfiguration setCellularDataLimit(int cellularDataLimit) {
        core().setCellularDataLimit(cellularDataLimit);
        return this;
    }

    public final int getDataUploadInterval() {
        return core().getDataUploadInterval();
    }

    public final TestTrackConfiguration setDataUploadInterval(int dataUploadInterval) {
        core().setDataUploadInterval(dataUploadInterval);
        return this;
    }

    public final int getSessionInterval() {
        return core().getSessionInterval();
    }

    public final TestTrackConfiguration setSessionInterval(int sessionInterval) {
        core().setSessionInterval(sessionInterval);
        return this;
    }

    public final String getDataCollectionServerHost() {
        return core().getDataCollectionServerHost();
    }

    public final TestTrackConfiguration setDataCollectionServerHost(String dataCollectionServerHost) {
        core().setDataCollectionServerHost(dataCollectionServerHost);
        return this;
    }

    public final boolean isOaidEnabled() {
        return core().isOaidEnabled();
    }

    public final TestTrackConfiguration setOaidEnabled(boolean enabled) {
        core().setOaidEnabled(enabled);
        return this;
    }
}
