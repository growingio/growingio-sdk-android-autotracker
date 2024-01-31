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

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.CdpConfig;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

public class CdpDowngradeProvider implements TrackerLifecycleProvider {
    private static final String TAG = "CdpDowngradeProvider";
    private ConfigurationProvider configurationProvider;

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        CdpConfig config = configurationProvider.getConfiguration(CdpConfig.class);
        if (config.isDowngrade()) {
            downgrade();
        }
    }

    /**
     * Downgrade the autotrack config to the version before 4.0.0.
     */
    private void downgrade() {
        AutotrackConfig config = configurationProvider.getConfiguration(AutotrackConfig.class);
        config.getAutotrackOptions().setFragmentPageEnabled(true);
        config.getAutotrackOptions().setActivityPageEnabled(true);
    }

    @Override
    public void shutdown() {

    }
}
