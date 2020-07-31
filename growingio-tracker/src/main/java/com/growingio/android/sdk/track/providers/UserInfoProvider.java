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

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.ErrorLog;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.ipc.GrowingIOIPC;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.listener.OnUserIdChangedListener;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.ObjectUtils;

public interface UserInfoProvider {

    /**
     * @return 当前客户设置的UserId 即cs1
     */
    @GMainThread
    String getUserId();

    /**
     * 客户调用setUserId后会调用此方法
     *
     * @param userId 需要设置的userId， null -- 表示clearUserId
     */
    @GMainThread
    void setUserId(String userId);

    void registerUserIdChangedListener(OnUserIdChangedListener l);

    void unregisterUserIdChangedListener(OnUserIdChangedListener l);

    class UserInfoPolicy extends ListenerContainer<OnUserIdChangedListener, String> implements UserInfoProvider {
        private static final String TAG = "GIO.UserInfoProvider";
        private GrowingIOIPC mGrowingIOIPC;

        public UserInfoPolicy(CoreAppState coreAppState) {
            mGrowingIOIPC = coreAppState.getGrowingIOIPC();
        }

        public static UserInfoProvider get(@NonNull final CoreAppState coreAppState) {
            return GIOProviders.provider(UserInfoProvider.class, new GIOProviders.DefaultCallback<UserInfoProvider>() {
                @Override
                public UserInfoProvider value() {
                    return new UserInfoPolicy(coreAppState);
                }
            });
        }

        @Override
        public String getUserId() {
            return mGrowingIOIPC.getUserId();
        }

        @Override
        public void setUserId(String userId) {
            if (TextUtils.isEmpty(userId)) {
                // to null, never send visit, just return
                mGrowingIOIPC.setUserId(null);
                dispatchActions(null);
                return;
            }

            if (userId.length() > 1000) {
                LogUtil.e(TAG, ErrorLog.USER_ID_TOO_LONG);
                return;
            }

            // to non-null
            String oldUserId = getUserId();
            if (ObjectUtils.equals(userId, oldUserId)) {
                LogUtil.d(TAG, "setUserId, but the userId is same as the old userId, just return");
                return;
            }
            mGrowingIOIPC.setUserId(userId);
            dispatchActions(userId);
        }

        @Override
        public void registerUserIdChangedListener(OnUserIdChangedListener l) {
            register(l);
        }

        @Override
        public void unregisterUserIdChangedListener(OnUserIdChangedListener l) {
            unregister(l);
        }

        @Override
        protected void singleAction(OnUserIdChangedListener listener, String action) {
            listener.onUserIdChanged(action);
        }
    }
}
