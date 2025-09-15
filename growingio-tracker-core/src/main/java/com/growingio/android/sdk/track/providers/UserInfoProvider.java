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
package com.growingio.android.sdk.track.providers;

import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ObjectUtils;

public class UserInfoProvider implements TrackerLifecycleProvider {
    private static final String TAG = "UserInfoPolicy";

    private PersistentDataProvider persistentDataProvider;
    private SessionProvider sessionProvider;
    private ConfigurationProvider configurationProvider;

    UserInfoProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        persistentDataProvider = context.getProvider(PersistentDataProvider.class);
        sessionProvider = context.getProvider(SessionProvider.class);
    }

    @Override
    public void shutdown() {

    }


    public String getLoginUserKey() {
        return persistentDataProvider.getLoginUserKey();
    }

    public String getLoginUserId() {
        return persistentDataProvider.getLoginUserId();
    }

    @TrackThread
    public void setLoginUserId(String userId) {
        setLoginUserId(userId, null);
    }

    public void setLoginUserId(String userId, String userKey) {
        if (!configurationProvider.core().isIdMappingEnabled()) {
            if (userKey != null) {
                Logger.w(TAG, "setUserId with UserKey should enable idMapping in sdk Configuration.");
                userKey = null;
            }
        }

        // 考虑DataSharer存储限制
        if (userKey != null && userKey.length() > 1000) {
            Logger.e(TAG, "userKey max length is 1000.");
            return;
        }
        if (userId != null && userId.length() > 1000) {
            Logger.e(TAG, "userId max length is 1000.");
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            // to null, never send visit, just return
            persistentDataProvider.setLoginUserIdAndUserKey(null, null);
            Logger.d(TAG, "clean the userId (and will also clean the userKey");
            return;
        }

        String lastUserId = getLoginUserId();
        if (ObjectUtils.equals(userId, lastUserId)) {
            if (!ObjectUtils.equals(getLoginUserKey(), userKey)) {
                Logger.d(TAG, "setUserId, the userId=" + userId + " is same as the old userId, but the userKey=" + userKey + " is different.");
                persistentDataProvider.setLoginUserIdAndUserKey(userId, userKey);
            } else {
                Logger.d(TAG, "setUserId, the userId is same as the old userId, just return");
            }
            return;
        }

        Logger.d(TAG, "userIdChange: newUserId = " + userId + ", latestUserId = " + lastUserId);
        persistentDataProvider.setLoginUserIdAndUserKey(userId, (TextUtils.isEmpty(userKey) ? null : userKey));
        needSendVisit(userId);
    }

    @TrackThread
    private void needSendVisit(String newUserId) {
        String mLatestNonNullUserId = persistentDataProvider.getLatestNonNullUserId();
        if (newUserId != null && !newUserId.isEmpty()) {
            if (!TextUtils.isEmpty(mLatestNonNullUserId) && !newUserId.equals(mLatestNonNullUserId)) {
                Logger.d(TAG, "resend visit after UserIdChanged");
                sessionProvider.refreshSessionId();
                sessionProvider.generateVisit();
            }
            persistentDataProvider.setLatestNonNullUserId(newUserId);
        }
    }
}