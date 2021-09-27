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

import android.text.TextUtils;

import com.growingio.android.sdk.track.ErrorLog;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.listener.OnUserIdChangedListener;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ObjectUtils;

public class UserInfoProvider extends ListenerContainer<OnUserIdChangedListener, String> {
    private static final String TAG = "UserInfoPolicy";

    private static class SingleInstance {
        private static final UserInfoProvider INSTANCE = new UserInfoProvider();
    }

    private UserInfoProvider() {
    }

    public static UserInfoProvider get() {
        return SingleInstance.INSTANCE;
    }

    public String getLoginUserKey() {
        return PersistentDataProvider.get().getLoginUserKey();
    }

    public String getLoginUserId() {
        return PersistentDataProvider.get().getLoginUserId();
    }

    @TrackThread
    public void setLoginUserId(String userId) {
        setLoginUserId(userId, null);
    }

    public void setLoginUserId(String userId, String userKey) {
        if (!ConfigurationProvider.core().isIdMappingEnabled()) {
            userKey = null;
        }

        // 考虑DataSharer存储限制
        if (userKey != null && userKey.length() > 1000) {
            Logger.e(TAG, ErrorLog.USER_KEY_TOO_LONG);
            return;
        }
        if (userId != null && userId.length() > 1000) {
            Logger.e(TAG, ErrorLog.USER_ID_TOO_LONG);
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            // to null, never send visit, just return
            PersistentDataProvider.get().setLoginUserIdAndUserKey(null, null);
            Logger.d(TAG, "clean the userId (and will also clean the userKey");
            dispatchActions(null);
            return;
        }

        // to non-null
        if (ObjectUtils.equals(userId, getLoginUserId())) {
            if (!ObjectUtils.equals(getLoginUserKey(), userKey)) {
                PersistentDataProvider.get().setLoginUserIdAndUserKey(userId, userKey);
            }
            Logger.d(TAG, "setUserId, but the userId is same as the old userId, just return");
            return;
        }

        // 回调中通过getUserId获取oldUserId, 通过参数获取newUserId
        dispatchActions(userId);
        PersistentDataProvider.get().setLoginUserIdAndUserKey(userId, (TextUtils.isEmpty(userKey) ? null : userKey));
        needSendVisit(userId);
    }

    public void registerUserIdChangedListener(OnUserIdChangedListener l) {
        register(l);
    }

    public void unregisterUserIdChangedListener(OnUserIdChangedListener l) {
        unregister(l);
    }

    @Override
    protected void singleAction(OnUserIdChangedListener listener, String action) {
        listener.onUserIdChanged(action);
    }

    @TrackThread
    private void needSendVisit(String newUserId) {
        String mLatestNonNullUserId = PersistentDataProvider.get().getLatestNonNullUserId();
        Logger.d(TAG, "onUserIdChanged: newUserId = " + newUserId + ", mLatestNonNullUserId = " + mLatestNonNullUserId);
        if (!TextUtils.isEmpty(newUserId)) {
            if (TextUtils.isEmpty(mLatestNonNullUserId)) {
                SessionProvider.get().generateVisit();
            } else {
                if (!newUserId.equals(mLatestNonNullUserId)) {
                    SessionProvider.get().refreshSessionId();
                    SessionProvider.get().generateVisit();
                }
            }
            PersistentDataProvider.get().setLatestNonNullUserId(newUserId);
        }
    }
}