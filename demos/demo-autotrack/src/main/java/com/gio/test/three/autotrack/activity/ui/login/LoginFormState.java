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

package com.gio.test.three.autotrack.activity.ui.login;

import android.support.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private Integer mUsernameError;
    @Nullable
    private Integer mPasswordError;
    private boolean mIsDataValid;

    LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
        this.mUsernameError = usernameError;
        this.mPasswordError = passwordError;
        this.mIsDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.mUsernameError = null;
        this.mPasswordError = null;
        this.mIsDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return mUsernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return mPasswordError;
    }

    boolean isDataValid() {
        return mIsDataValid;
    }
}