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
package com.gio.test.three.autotrack.activity.ui.login;

import android.support.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private LoggedInUserView mSuccess;
    @Nullable
    private Integer mError;

    LoginResult(@Nullable Integer error) {
        this.mError = error;
    }

    LoginResult(@Nullable LoggedInUserView success) {
        this.mSuccess = success;
    }

    @Nullable
    LoggedInUserView getSuccess() {
        return mSuccess;
    }

    @Nullable
    Integer getError() {
        return mError;
    }
}