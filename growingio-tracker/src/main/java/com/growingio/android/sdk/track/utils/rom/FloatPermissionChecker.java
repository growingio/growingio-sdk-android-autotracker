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

package com.growingio.android.sdk.track.utils.rom;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * 1.默认策略为mCurrentRomChecker获取的intent无效则
 * 1.1 mCurrentRomChecker 为 CommonRomChecker 则校验对应的XXXRomChecker, 暂不支持, 目前已知的手机为meizu在23+依旧为自定义
 * 1.2 mCurrentRomChecker 为 XXXRomChecker 则校验对应的CommonRomChecker
 * 2.对intent做检查，防止intent无效导致崩溃
 */
public class FloatPermissionChecker {
    public static final String TAG = "FloatPermissionChecker";

    private RomPermissionChecker mCurrentRomChecker;
    private RomPermissionChecker mCommonRomChecker;

    public FloatPermissionChecker(RomPermissionChecker romChecker, RomPermissionChecker commonRomChecker) {
        mCurrentRomChecker = romChecker;
        mCommonRomChecker = commonRomChecker;
    }

    public @Nullable
    Intent getIntentOrNull() {
        Intent intent = null;
        intent = mCurrentRomChecker.getValidIntent();
        if (mCurrentRomChecker instanceof CommonRomChecker) {
            return intent;
        }
        return (intent != null) ? intent : mCommonRomChecker.getApplyPermissionIntent();
    }

    public boolean checkOp() {
        return mCurrentRomChecker.check();
    }

    public static class Builder {
        private RomPermissionChecker mCurrentRomChecker;
        private RomPermissionChecker mCommonRomChecker;

        public Builder(Activity context) {
            mCurrentRomChecker = FloatPermissionUtil.getPermissionChecker(context);
            mCommonRomChecker = new CommonRomChecker(context);
        }

        public FloatPermissionChecker build() {
            return new FloatPermissionChecker(mCurrentRomChecker, mCommonRomChecker);
        }

        public Builder setCurrentRomChecker(RomPermissionChecker currentRomChecker) {
            this.mCurrentRomChecker = currentRomChecker;
            return this;
        }
    }
}
