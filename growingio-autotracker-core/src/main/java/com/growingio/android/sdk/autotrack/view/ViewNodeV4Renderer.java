/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.view;

import static com.growingio.android.sdk.autotrack.view.PageHelper.POPUP_DECOR_VIEW_CLASS_NAME;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.WindowPage;
import com.growingio.android.sdk.autotrack.shadow.ListMenuItemViewShadow;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.hybrid.HybridDom;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * @author cpacm 2023/7/10
 */
class ViewNodeV4Renderer implements ViewNodeRenderer {
    @Override
    public void generateMenuItemEvent(Activity activity, MenuItem menuItem) {
        Page<?> page = PageProvider.get().searchActivityPage(activity);
        ViewNodeV4 viewNode = renderMenuItemViewNode(activity, menuItem);
        if (page != null && viewNode != null) {
            StringBuilder xpath = new StringBuilder();
            StringBuilder xIndex = new StringBuilder();
            String pagePath = page.originPath(false);
            xpath.append(pagePath).append(viewNode.getXPath());
            xIndex.append(page.getXIndex()).append(viewNode.getXIndex());

            TrackMainThread.trackMain().
                    postEventToTrackMain(
                            new ViewElementEvent.Builder(AutotrackEventType.VIEW_CLICK)
                                    .setPath(page.activePath())
                                    .setXIndex(xpath.toString())
                                    .setXpath(xpath.toString())
                                    .setIndex(viewNode.getIndex())
                                    .setTextValue(viewNode.getViewContent())
                                    .setAttributes(page.activeAttributes()));
        } else {
            Logger.e(TAG, "Page or MenuItem ViewNode is NULL");
        }
    }

    private ViewNodeV4 renderMenuItemViewNode(Context context, MenuItem menuItem) {
        return ViewNodeV4.generateMenuItemViewNode(context, menuItem);
    }

    @Override
    public void generateViewClickEvent(View view) {
        if (view == null) {
            Logger.e(TAG, "View is NULL");
            return;
        }
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null || ViewAttributeUtil.isIgnoredView(view)) {
            Logger.e(TAG, "View is ignored");
            return;
        }

        ViewNodeV4 viewNode = renderViewNode(view);
        if (viewNode != null) {
            Page<?> page = PageProvider.get().findPage(view);
            if (page == null) {
                Logger.w(TAG, "sendClickEvent: page is NULL");
                return;
            }

            StringBuilder xpath = new StringBuilder();
            StringBuilder xIndex = new StringBuilder();
            String pagePath = page.originPath(false);
            xpath.append(pagePath).append(viewNode.getXPath());
            xIndex.append(page.getXIndex()).append(viewNode.getXIndex());

            if (viewNode.getViewContent() == null || viewNode.getViewContent().isEmpty()) {
                String content = ViewAttributeUtil.findViewContent(viewNode.getView());
                viewNode.setViewContent(content);
            }

            TrackMainThread.trackMain().postEventToTrackMain(
                    new ViewElementEvent.Builder(AutotrackEventType.VIEW_CLICK)
                            .setPath(page.activePath())
                            .setXpath(xpath.toString())
                            .setXIndex(xIndex.toString())
                            .setIndex(viewNode.getIndex())
                            .setTextValue(viewNode.getViewContent())
                            .setAttributes(page.activeAttributes())
            );
        } else {
            Logger.e(TAG, "ViewNode is NULL");
        }
    }

    @Override
    public void generateViewChangeEvent(View view) {
        if (view == null) {
            Logger.e(TAG, "View is NULL");
            return;
        }
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null || ViewAttributeUtil.isIgnoredView(view)) {
            Logger.e(TAG, "View is ignored");
            return;
        }

        if (!ViewAttributeUtil.hasEditTextChanged(view)) {
            return;
        }

        ViewNodeV4 viewNode = renderViewNode(view);
        if (viewNode != null) {
            Page<?> page = PageProvider.get().findPage(view);
            if (page == null) {
                Logger.w(TAG, "sendChangeEvent: page is NULL");
                return;
            }
            StringBuilder xpath = new StringBuilder();
            StringBuilder xIndex = new StringBuilder();
            String pagePath = page.originPath(false);
            xpath.append(pagePath).append(viewNode.getXPath());
            xIndex.append(page.getXIndex()).append(viewNode.getXIndex());

            if (viewNode.getViewContent() == null || viewNode.getViewContent().isEmpty()) {
                String content = ViewAttributeUtil.findViewContent(viewNode.getView());
                viewNode.setViewContent(content);
            }

            TrackMainThread.trackMain().postEventToTrackMain(
                    new ViewElementEvent.Builder(AutotrackEventType.VIEW_CHANGE)
                            .setPath(page.activePath())
                            .setXpath(xpath.toString())
                            .setXIndex(xIndex.toString())
                            .setIndex(viewNode.getIndex())
                            .setTextValue(viewNode.getViewContent())
                            .setAttributes(page.activeAttributes())
            );
        } else {
            Logger.e(TAG, "ViewNode is NULL");
        }
    }

    ViewNodeV4 renderViewNode(View childView) {
        // judge Menu Item
        if (ListMenuItemViewShadow.isListMenuItemView(childView) && childView.getContext() != null) {
            MenuItem menuItem = new ListMenuItemViewShadow(childView).getMenuItem();
            if (menuItem != null) {
                return renderMenuItemViewNode(childView.getContext(), menuItem);
            }
        }

        // calculate ViewNode LinkedNodeTree
        return makeNodeLinkTree(childView);
    }

    /**
     * make one-way NodeTree
     *
     * @param childView clicked View
     * @return childView's ViewNode
     */
    private ViewNodeV4 makeNodeLinkTree(View childView) {
        // find linked views
        LinkedList<View> linkedViews = new LinkedList<>();
        ViewNodeV4 childViewNode = findRootViewNode(childView, linkedViews);

        while (!linkedViews.isEmpty()) {
            childViewNode = childViewNode.append(linkedViews.pollFirst());
        }
        return childViewNode;
    }

    private ViewNodeV4 findRootViewNode(View childView, LinkedList<View> linkedViews) {
        View rootView = childView;
        Page<?> findPage;
        do {
            findPage = ViewAttributeUtil.getViewPage(rootView);
            linkedViews.addFirst(rootView);
            // 找到page直接迭代page
            if (findPage != null) {
                break;
            }
            if (rootView.getParent() instanceof View) {
                rootView = (View) rootView.getParent();
            } else {
                break;
            }

        } while (rootView instanceof ViewGroup);

        StringBuilder xpath = new StringBuilder();
        StringBuilder xIndex = new StringBuilder();

        if (findPage != null) {
            xpath.append("/").append(ClassUtil.getSimpleClassName(rootView.getClass()));
            if (ViewAttributeUtil.getCustomId(rootView) != null) {
                xIndex.append("/").append(ViewAttributeUtil.getCustomId(rootView));
            } else {
                xIndex.append("/0");
            }
        } else {
            String prefix = PageHelper.getWindowPrefix(rootView);
            findPage = new WindowPage(prefix);
            Page parentPage = PageProvider.get().findPage(rootView);
            findPage.assignParent(parentPage);
            // PopupDecorView 这个class是在Android 6.0的时候才引入的，为了兼容6.0以下的版本，需要手动添加这个class层级
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && prefix.equals(PageHelper.POPUP_WINDOW_PREFIX) && !POPUP_DECOR_VIEW_CLASS_NAME.equals(ClassUtil.getSimpleClassName(rootView.getClass()))) {
                xpath.append("/").append(POPUP_DECOR_VIEW_CLASS_NAME).append("/").append(ClassUtil.getSimpleClassName(rootView.getClass()));
                xIndex.append("/0/0");
            }
        }
        // construct root viewNode
        rootView = linkedViews.pollFirst();
        return new ViewNodeV4().withView(rootView)
                .setIndex(-1)
                .setPage(findPage)
                .setViewContent(ViewAttributeUtil.getViewContent(rootView))
                .setXPath(xpath.toString())
                .setXIndex(xIndex.toString())
                .setIndeedXIndex(xIndex.toString());
    }

    @Override
    public JSONArray buildScreenPages(List<DecorView> decorViews) {
        JSONArray container = new JSONArray();
        for (DecorView decorView : decorViews) {
            if (decorView.getView() instanceof TipView) {
                continue;
            }
            if (ViewAttributeUtil.isViewInvisible(decorView.getView())) {
                continue;
            }
            checkView2PageElement(decorView.getView(), container);
        }
        return container;
    }

    private void checkView2PageElement(View view, JSONArray container) {
        Page<?> viewPage = ViewAttributeUtil.getViewPage(view);
        if (viewPage != null) {
            if (viewPage.isAutotrack()) {
                JSONObject pageJson = ScreenElementHelper.createPageElementData(view, viewPage);
                if (pageJson != null) {
                    container.put(pageJson);
                }
            }
            for (Page page : viewPage.getAllChildren()) {
                if (page.isAutotrack()) {
                    JSONObject childPageJson = ScreenElementHelper.createPageElementData(view, page);
                    if (childPageJson != null) {
                        container.put(childPageJson);
                    }
                }
            }
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.getChildCount() > 0) {
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    checkView2PageElement(viewGroup.getChildAt(i), container);
                }
            }
        }
    }

    @Override
    public JSONArray buildScreenViews(List<DecorView> decorViews) {
        JSONArray container = new JSONArray();
        for (DecorView decorView : decorViews) {
            if (decorView.getView() instanceof TipView) {
                continue;
            }
            if (ViewAttributeUtil.isViewInvisible(decorView.getView())) {
                continue;
            }
            checkView2ViewElement(decorView.getView(), container);
        }
        return container;
    }

    private void checkView2ViewElement(View view, JSONArray container) {
        ViewNodeV4 topViewNode = findRootViewNode(view, new LinkedList<>());
        traverseViewNode(topViewNode, container);
    }

    private void traverseViewNode(ViewNodeV4 viewNode, JSONArray container) {
        if (ViewAttributeUtil.isViewInvisible(viewNode.getView())) return;

        // deal with WebView dom.
        if (ClassExistHelper.isWebView(viewNode.getView()) && ViewNodeProvider.get().getHybridModelLoader() != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            disposeWebView(viewNode, container, latch);
            try {
                //noinspection ResultOfMethodCallIgnored
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        } else if (ViewUtil.canCircle(viewNode.getView())) {
            if (viewNode.getViewContent() == null || viewNode.getViewContent().isEmpty()) {
                String content = ViewAttributeUtil.findViewContent(viewNode.getView());
                viewNode.setViewContent(content);
            }
            container.put(ScreenElementHelper.createViewElementData(viewNode, container.length(), null));
        }

        if (viewNode.getView() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) viewNode.getView();
            if (viewGroup.getChildCount() > 0) {
                for (int index = 0; index < viewGroup.getChildCount(); index++) {
                    ViewNodeV4 childViewNode = viewNode.append(viewGroup.getChildAt(index), index);
                    traverseViewNode(childViewNode, container);
                }
            }
        }
    }

    private void disposeWebView(ViewNodeV4 viewNode, JSONArray container, final CountDownLatch latch) {
        ModelLoader<HybridDom, HybridJson> modelLoader = ViewNodeProvider.get().getHybridModelLoader();
        if (modelLoader == null) {
            latch.countDown();
            return;
        }
        viewNode.getView().post(() -> {
            LoadDataFetcher<HybridJson> loadDataFetcher = (LoadDataFetcher<HybridJson>) modelLoader.buildLoadData(new HybridDom(viewNode.getView())).fetcher;
            loadDataFetcher.loadData(new LoadDataFetcher.DataCallback<HybridJson>() {
                @Override
                public void onDataReady(HybridJson data) {
                    viewNode.setViewContent("WebView");
                    JSONObject webViewJson = ScreenElementHelper.createViewElementData(viewNode, container.length(), data);
                    container.put(webViewJson);
                    latch.countDown();
                }

                @Override
                public void onLoadFailed(Exception e) {
                    latch.countDown();
                }
            });
        });
    }


}
