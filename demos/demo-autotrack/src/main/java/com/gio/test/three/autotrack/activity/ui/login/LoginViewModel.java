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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Patterns;

import com.gio.test.three.autotrack.R;
import com.gio.test.three.autotrack.activity.data.LoginRepository;
import com.gio.test.three.autotrack.activity.data.Result;
import com.gio.test.three.autotrack.activity.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> mLoginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> mLoginResult = new MutableLiveData<>();
    private LoginRepository mLoginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.mLoginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return mLoginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return mLoginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = mLoginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            mLoginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            mLoginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            mLoginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            mLoginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            mLoginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}