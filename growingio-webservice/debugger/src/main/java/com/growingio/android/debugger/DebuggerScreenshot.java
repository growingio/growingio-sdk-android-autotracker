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
package com.growingio.android.debugger;

import android.util.DisplayMetrics;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.async.UnsubscribedDisposable;
import com.growingio.android.sdk.track.utils.DeviceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * create screenshot server date from app's screenshot for debugger
 */
public class DebuggerScreenshot {
    private static final String MSG_TYPE = "refreshScreenshot";

    private final int mScreenWidth;
    private final int mScreenHeight;
    private final float mScale;
    private final String mScreenshot;
    private final String mMsgType;
    private final long mSnapshotKey;

    public DebuggerScreenshot(Builder builder) {
        mMsgType = MSG_TYPE;
        mScreenWidth = builder.mScreenWidth;
        mScreenHeight = builder.mScreenHeight;
        mScale = builder.mScale;
        mScreenshot = builder.mScreenshot;
        mSnapshotKey = builder.mSnapshotKey;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("screenWidth", mScreenWidth);
            json.put("screenHeight", mScreenHeight);
            json.put("scale", mScale);
            json.put("screenshot", mScreenshot);
            json.put("msgType", mMsgType);
            json.put("snapshotKey", mSnapshotKey);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static final class Builder {
        private int mScreenWidth;
        private int mScreenHeight;
        private float mScale;
        private String mScreenshot;
        private long mSnapshotKey;
        private Callback<DebuggerScreenshot> mScreenshotResultCallback;
        private Disposable mBuildDisposable;

        public Builder setScale(float scale) {
            mScale = scale;
            return this;
        }

        public Builder setScreenshot(String screenshot) {
            mScreenshot = screenshot;
            return this;
        }

        public Builder setSnapshotKey(long snapshotKey) {
            mSnapshotKey = snapshotKey;
            return this;
        }

        public Disposable build(Callback<DebuggerScreenshot> callback) {
            if (callback == null) {
                return Disposable.EMPTY_DISPOSABLE;
            }
            mBuildDisposable = new UnsubscribedDisposable();
            mScreenshotResultCallback = callback;

            DisplayMetrics displayMetrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
            mScreenWidth = displayMetrics.widthPixels;
            mScreenHeight = displayMetrics.heightPixels;

            callResultOnSuccess();
            return mBuildDisposable;
        }

        private void callResultOnSuccess() {
            if (!mBuildDisposable.isDisposed()) {
                mBuildDisposable.dispose();
                if (mScreenshotResultCallback != null) {
                    mScreenshotResultCallback.onSuccess(new DebuggerScreenshot(this));
                }
            }
        }

        private void callResultOnFailed() {
            if (!mBuildDisposable.isDisposed()) {
                mBuildDisposable.dispose();
                if (mScreenshotResultCallback != null) {
                    mScreenshotResultCallback.onFailed();
                }
            }
        }

    }
}
