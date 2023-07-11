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

package com.growingio.android.circler.screenshot;

import android.util.DisplayMetrics;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.view.ViewNodeProvider;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.view.WindowHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * create screenshot server date from app's screenshot for circle and debugger
 */
public class CircleScreenshot {
    private static final String MSG_TYPE = "refreshScreenshot";

    private final int mScreenWidth;
    private final int mScreenHeight;
    private final float mScale;
    private final String mScreenshot;
    private final String mMsgType;
    private final long mSnapshotKey;
    private final JSONArray mElements;
    private final JSONArray mPages;

    public CircleScreenshot(Builder builder) {
        mMsgType = MSG_TYPE;
        mScreenWidth = builder.mScreenWidth;
        mScreenHeight = builder.mScreenHeight;
        mScale = builder.mScale;
        mScreenshot = builder.mScreenshot;
        mSnapshotKey = builder.mSnapshotKey;
        mElements = builder.mViewElements;
        mPages = builder.mPages;
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
            json.put("elements", mElements);
            json.put("pages", mPages);
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
        private final JSONArray mViewElements = new JSONArray();
        private final JSONArray mPages = new JSONArray();
        private Callback<CircleScreenshot> mScreenshotResultCallback;

        public Builder() {
            DisplayMetrics displayMetrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
            mScreenWidth = displayMetrics.widthPixels;
            mScreenHeight = displayMetrics.heightPixels;
        }

        public Builder setScreenWidth(int screenWidth) {
            this.mScreenWidth = screenWidth;
            return this;
        }

        public Builder setScreenHeight(int screenHeight) {
            this.mScreenHeight = screenHeight;
            return this;
        }

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

        public Builder addElement(JSONObject element) {
            mViewElements.put(element);
            return this;
        }

        public Builder addPage(JSONObject page) {
            mPages.put(page);
            return this;
        }

        public void build(Callback<CircleScreenshot> callback) {
            List<DecorView> decorViews = WindowHelper.get().getTopActivityViews();
            build(decorViews, callback);
        }

        public void build(List<DecorView> decorViews, Callback<CircleScreenshot> callback) {
            if (callback == null) {
                return;
            }

            if (decorViews == null || decorViews.isEmpty()) {
                callResultOnFailed();
                return;
            }

            mScreenshotResultCallback = callback;

            try {
                JSONArray p = ViewNodeProvider.get().buildScreenPages(decorViews);
                for (int i = 0; i < p.length(); i++) {
                    mPages.put(p.getJSONObject(i));
                }
                JSONArray v = ViewNodeProvider.get().buildScreenViews(decorViews);
                for (int i = 0; i < v.length(); i++) {
                    mViewElements.put(v.getJSONObject(i));
                }
            } catch (JSONException ignored) {

            }
            callResultOnSuccess();
        }

        private void callResultOnSuccess() {
            if (mScreenshotResultCallback != null) {
                mScreenshotResultCallback.onSuccess(new CircleScreenshot(this));
            }
        }

        private void callResultOnFailed() {
            if (mScreenshotResultCallback != null) {
                mScreenshotResultCallback.onFailed();
            }
        }
    }
}
