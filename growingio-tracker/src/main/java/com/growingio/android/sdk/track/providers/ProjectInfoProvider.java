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

package com.growingio.android.sdk.track.providers;

import android.content.Context;
import android.text.TextUtils;

import com.growingio.android.sdk.track.utils.GIOProviders;

/**
 * 用于获取设置的项目信息
 */
public interface ProjectInfoProvider {

    /**
     * @return 返回项目的projectId
     */
    String getProjectId();

    ProjectInfoProvider setProjectId(String projectId);

    /**
     * @return 返回项目的urlScheme
     */
    String getUrlScheme();

    ProjectInfoProvider setUrlScheme(String urlScheme);

    /**
     * @return 返回项目的渠道名
     */
    String getChannel(Context context);

    ProjectInfoProvider setChannel(String channel);

    String getPackageName(Context context);

    class AccountInfoPolicy implements ProjectInfoProvider {

        private String mProjectId;
        private String mUrlScheme;
        private String mChannel;

        public static ProjectInfoProvider get() {
            return GIOProviders.provider(ProjectInfoProvider.class, new GIOProviders.DefaultCallback<ProjectInfoProvider>() {
                @Override
                public ProjectInfoProvider value() {
                    return new AccountInfoPolicy();
                }
            });
        }

        @Override
        public String getProjectId() {
            return mProjectId;
        }

        @Override
        public ProjectInfoProvider setProjectId(String projectId) {
            this.mProjectId = projectId;
            return this;
        }

        @Override
        public String getUrlScheme() {
            return mUrlScheme;
        }

        @Override
        public ProjectInfoProvider setUrlScheme(String urlScheme) {
            this.mUrlScheme = urlScheme;
            return this;
        }

        @Override
        public String getChannel(Context context) {
            if (TextUtils.isEmpty(mChannel)) {
                // 读取channel
            }
            return mChannel;
        }

        @Override
        public ProjectInfoProvider setChannel(String channel) {
            this.mChannel = channel;
            return this;
        }

        @Override
        public String getPackageName(Context context) {
            return context.getPackageName();
        }

        private String readResource(Context context, String resName) {
            try {
                return context.getString(context.getResources().getIdentifier(resName, "string", context.getPackageName()));
            } catch (Exception e) {
                return null;
            }
        }
    }
}
