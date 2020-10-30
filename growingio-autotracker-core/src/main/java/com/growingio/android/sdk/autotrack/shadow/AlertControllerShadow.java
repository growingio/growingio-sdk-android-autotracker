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

package com.growingio.android.sdk.autotrack.shadow;

import android.app.AlertDialog;
import android.support.annotation.Nullable;

import com.growingio.android.sdk.autotrack.util.HurtLocker;
import com.growingio.android.sdk.track.log.Logger;

public class AlertControllerShadow {
    private static final String TAG = "AlertControllerShadow";

    private final Object mRealAlertController;

    public AlertControllerShadow(AlertDialog alertDialog) throws Exception {
        mRealAlertController = HurtLocker.getInternalState(alertDialog, "mAlert");
    }

    @Nullable
    public CharSequence getTitle() {
        if (mRealAlertController != null) {
            try {
                return HurtLocker.getInternalState(mRealAlertController, "mTitle");
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return null;
    }

    @Nullable
    public CharSequence getMessage() {
        if (mRealAlertController != null) {
            try {
                return HurtLocker.getInternalState(mRealAlertController, "mMessage");
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return null;
    }
}
