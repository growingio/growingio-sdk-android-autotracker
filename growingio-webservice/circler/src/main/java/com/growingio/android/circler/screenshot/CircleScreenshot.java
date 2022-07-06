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
import android.view.View;
import android.view.ViewGroup;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewNode;
import com.growingio.android.sdk.autotrack.view.ViewUtil;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.hybrid.HybridDom;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.view.WindowHelper;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * create screenshot server date from app's screenshot  for circle and debugger
 */
public class CircleScreenshot {
    private static final String MSG_TYPE = "refreshScreenshot";

    private final int mScreenWidth;
    private final int mScreenHeight;
    private final float mScale;
    private final String mScreenshot;
    private final String mMsgType;
    private final long mSnapshotKey;
    private final List<ViewElement> mElements;
    private final List<PageElement> mPages;

    public CircleScreenshot(Builder builder) {
        mMsgType = MSG_TYPE;
        mScreenWidth = builder.mScreenWidth;
        mScreenHeight = builder.mScreenHeight;
        mScale = builder.mScale;
        mScreenshot = builder.mScreenshot;
        mSnapshotKey = builder.mSnapshotKey;
        mElements = Collections.unmodifiableList(builder.mViewElements);
        mPages = Collections.unmodifiableList(builder.mPages);
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

            JSONArray elementArray = new JSONArray();
            for (ViewElement element : mElements) {
                elementArray.put(element.toJSONObject());
            }
            json.put("elements", elementArray);

            JSONArray pageArray = new JSONArray();
            for (PageElement page : mPages) {
                pageArray.put(page.toJSONObject());
            }
            json.put("pages", pageArray);
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
        private final List<ViewElement> mViewElements = new ArrayList<>();
        private final List<PageElement> mPages = new ArrayList<>();
        private final AtomicInteger mScreenLock = new AtomicInteger(0);
        private int mViewCount = 0;
        private Callback<CircleScreenshot> mScreenshotResultCallback;

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

            DisplayMetrics displayMetrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
            mScreenWidth = displayMetrics.widthPixels;
            mScreenHeight = displayMetrics.heightPixels;

            mScreenLock.incrementAndGet();
            for (DecorView decorView : decorViews) {
                if (decorView.getView() instanceof TipView) {
                    continue;
                }
                if (isViewInvisible(decorView.getView())) {
                    continue;
                }
                checkView2PageElement(decorView.getView());
                checkView2ViewElement(decorView.getView());
            }
            callResultOnSuccess();
        }

        private ViewElement.Builder createViewElementBuilder(ViewNode viewNode) {
            ViewElement.Builder builder = new ViewElement.Builder();
            int[] location = new int[2];
            viewNode.getView().getLocationOnScreen(location);

            return builder.setLeft(location[0])
                    .setTop(location[1])
                    .setHeight(viewNode.getView().getHeight())
                    .setWidth(viewNode.getView().getWidth())
                    .setContent(viewNode.getViewContent())
                    .setNodeType(viewNode.getNodeType())
                    .setPage(PageProvider.get().findPage(viewNode.getView()).path())
                    .setParentXPath(viewNode.getClickableParentXPath())
                    .setXpath(viewNode.getXPath())
                    .setIndex(viewNode.getIndex())
                    .setZLevel(mViewCount++);
        }

        private void checkView2ViewElement(View view) {
            ViewNode topViewNode = ViewHelper.getTopViewNode(view, null);
            traverseViewNode(topViewNode);
        }

        private void traverseViewNode(ViewNode viewNode) {
            if (isViewInvisible(viewNode.getView())) return;
            if (!disposeWebView(viewNode) && ViewUtil.canCircle(viewNode.getView())) {
                mViewElements.add(createViewElementBuilder(viewNode).build());
            }
            if (viewNode.getView() instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewNode.getView();
                if (viewGroup.getChildCount() > 0) {
                    for (int index = 0; index < viewGroup.getChildCount(); index++) {
                        ViewNode childViewNode = viewNode.appendNode(viewGroup.getChildAt(index), index);
                        traverseViewNode(childViewNode);
                    }
                }
            }
        }

        private boolean disposeWebView(ViewNode viewNode) {
            if (ClassExistHelper.isWebView(viewNode.getView())) {
                ModelLoader<HybridDom, HybridJson> modelLoader = ScreenshotProvider.get().getHybridModelLoader();
                if (modelLoader == null) return false;
                mScreenLock.incrementAndGet();
                LoadDataFetcher<HybridJson> loadDataFetcher = (LoadDataFetcher<HybridJson>) modelLoader.buildLoadData(new HybridDom(viewNode.getView())).fetcher;
                loadDataFetcher.loadData(new LoadDataFetcher.DataCallback<HybridJson>() {
                    @Override
                    public void onDataReady(HybridJson data) {
                        ViewElement.Builder elementBuilder = createViewElementBuilder(viewNode);
                        mViewElements.add(elementBuilder.setWebView(data.getJsonObject()).build());
                        callResultOnSuccess();
                    }

                    @Override
                    public void onLoadFailed(Exception e) {
                        callResultOnSuccess();
                    }
                });
                return true;
            }
            return false;
        }

        private void callResultOnSuccess() {
            if (mScreenLock.decrementAndGet() <= 0) {
                if (mScreenshotResultCallback != null) {
                    mScreenshotResultCallback.onSuccess(new CircleScreenshot(this));
                }
            }
        }

        private void callResultOnFailed() {
            if (mScreenshotResultCallback != null) {
                mScreenshotResultCallback.onFailed();
            }
        }

        private void checkView2PageElement(View view) {
            Page<?> viewPage = ViewAttributeUtil.getViewPage(view);
            if (viewPage != null) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);

                mPages.add(new PageElement.Builder()
                        .setTitle(viewPage.getTitle())
                        .setPath(viewPage.path())
                        .setIgnored(viewPage.isIgnored())
                        .setHeight(view.getHeight())
                        .setWidth(view.getWidth())
                        .setLeft(location[0])
                        .setTop(location[1])
                        .build());
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                if (viewGroup.getChildCount() > 0) {
                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                        checkView2PageElement(viewGroup.getChildAt(i));
                    }
                }
            }
        }

        private boolean isViewInvisible(View view) {
            return view.getVisibility() == View.GONE || view.getWidth() <= 0 || view.getHeight() <= 0;
        }
    }
}
