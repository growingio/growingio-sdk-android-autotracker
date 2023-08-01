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
package com.growingio.android.sdk.autotrack.view;

import android.view.View;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2023/7/11
 */
public class ScreenElementHelper {

    private ScreenElementHelper() {
    }

    private static final String VIEW_XPATH = "xpath";
    private static final String VIEW_PARENT_XPATH = "parentXPath";
    private static final String VIEW_PARENT_XINDEX = "parentXIndex"; //new for v4
    private static final String VIEW_XINDEX = "xindex"; //new for v4
    private static final String VIEW_LEFT = "left";
    private static final String VIEW_TOP = "top";
    private static final String VIEW_WIDTH = "width";
    private static final String VIEW_HEIGHT = "height";
    private static final String VIEW_NODE_TYPE = "nodeType";
    private static final String VIEW_CONTENT = "content";
    private static final String VIEW_PAGE = "page"; // for 3.0
    private static final String VIEW_PATH = "path"; //new for v4.0, replace with page
    private static final String VIEW_Z_LEVEL = "zLevel";
    private static final String VIEW_INDEX = "index";
    private static final String VIEW_WEBVIEW = "webView";

    /**
     * data from flutter or ReactNative etc.
     */
    public static JSONObject createViewElementWithMap(Map<String, Object> data) {
        try {
            JSONObject json = new JSONObject();
            json.put(VIEW_XPATH, (String) data.get(VIEW_XPATH));
            if (data.containsKey(VIEW_XINDEX)) {
                json.put(VIEW_XINDEX, (String) data.get(VIEW_XINDEX));
            }
            json.put(VIEW_PARENT_XPATH, (String) data.get(VIEW_PARENT_XPATH));
            if (data.containsKey(VIEW_PARENT_XINDEX)) {
                json.put(VIEW_PARENT_XINDEX, (String) data.get(VIEW_PARENT_XINDEX));
            }
            json.put(VIEW_LEFT, ((Double) data.get(VIEW_LEFT)).intValue());
            json.put(VIEW_TOP, ((Double) data.get(VIEW_TOP)).intValue());
            json.put(VIEW_WIDTH, ((Double) data.get(VIEW_WIDTH)).intValue());
            json.put(VIEW_HEIGHT, ((Double) data.get(VIEW_HEIGHT)).intValue());
            json.put(VIEW_NODE_TYPE, (String) data.get(VIEW_NODE_TYPE));
            json.put(VIEW_CONTENT, (String) data.get(VIEW_CONTENT));
            json.put(VIEW_PAGE, (String) data.get(VIEW_PAGE));
            json.put(VIEW_Z_LEVEL, (int) data.get(VIEW_Z_LEVEL));
            int index = (int) data.get(VIEW_INDEX);
            if (index > -1) {
                json.put(VIEW_INDEX, index);
            }
            return json;
        } catch (Exception e) {
            Logger.e("Circler Data ViewElement parser fail", e);
        }
        return null;
    }

    static JSONObject createViewElementData(ViewNodeV3 viewNode, int zLevel, HybridJson webViewData) {
        int[] location = new int[2];
        viewNode.getView().getLocationOnScreen(location);
        JSONObject json = new JSONObject();
        try {
            json.put(VIEW_XPATH, viewNode.getXPath());
            json.put(VIEW_PARENT_XPATH, viewNode.getClickableParentXPath());
            json.put(VIEW_LEFT, location[0]);
            json.put(VIEW_TOP, location[1]);
            json.put(VIEW_WIDTH, viewNode.getView().getWidth());
            json.put(VIEW_HEIGHT, viewNode.getView().getHeight());
            json.put(VIEW_NODE_TYPE, viewNode.getNodeType());
            json.put(VIEW_CONTENT, viewNode.getViewContent());
            json.put(VIEW_PAGE, viewNode.findPagePath());
            json.put(VIEW_Z_LEVEL, zLevel);
            if (viewNode.getIndex() > -1) {
                json.put(VIEW_INDEX, viewNode.getIndex());
            }
            if (webViewData != null && webViewData.getJsonObject() != null) {
                json.put(VIEW_WEBVIEW, webViewData.getJsonObject());
            }

        } catch (JSONException ignored) {
        }
        return json;
    }

    static JSONObject createViewElementData(ViewNodeV4 viewNode, int zLevel, HybridJson webViewData) {
        int[] location = new int[2];
        viewNode.getView().getLocationOnScreen(location);
        JSONObject json = new JSONObject();

        Page page = viewNode.getPage();
        if (page == null) return null;
        try {
            String pagePath = page.originPath(false);
            json.put(VIEW_PATH, page.activePath());
            json.put(VIEW_XPATH, pagePath + viewNode.getXPath());
            json.put(VIEW_PARENT_XPATH, viewNode.getClickableParentXPath() == null ? null : pagePath + viewNode.getClickableParentXPath());
            json.put(VIEW_PARENT_XINDEX, viewNode.getClickablePatentXIndex() == null ? null : page.getXIndex() + viewNode.getClickablePatentXIndex());
            json.put(VIEW_XINDEX, page.getXIndex() + viewNode.getXIndex());
            json.put(VIEW_LEFT, location[0]);
            json.put(VIEW_TOP, location[1]);
            json.put(VIEW_WIDTH, viewNode.getView().getWidth());
            json.put(VIEW_HEIGHT, viewNode.getView().getHeight());
            json.put(VIEW_NODE_TYPE, viewNode.getNodeType());
            json.put(VIEW_CONTENT, viewNode.getViewContent());
            json.put(VIEW_Z_LEVEL, zLevel);
            if (viewNode.getIndex() > -1) {
                json.put(VIEW_INDEX, viewNode.getIndex());
            }
            if (webViewData != null && webViewData.getJsonObject() != null) {
                json.put(VIEW_WEBVIEW, webViewData.getJsonObject());
            }

        } catch (JSONException ignored) {
        }
        return json;
    }


    private static final String PAGE_PATH = "path";
    private static final String PAGE_TITLE = "title";
    private static final String PAGE_LEFT = "left";
    private static final String PAGE_TOP = "top";
    private static final String PAGE_WIDTH = "width";
    private static final String PAGE_HEIGHT = "height";
    private static final String PAGE_IGNORED = "isIgnored";

    public static JSONObject createPageElementWithMap(Map<String, Object> data) {
        try {
            JSONObject json = new JSONObject();
            json.put(PAGE_PATH, (String) data.get("path"));
            json.put(PAGE_TITLE, (String) data.get("title"));
            json.put(PAGE_LEFT, ((Double) data.get("left")).intValue());
            json.put(PAGE_TOP, ((Double) data.get("top")).intValue());
            json.put(PAGE_WIDTH, ((Double) data.get("width")).intValue());
            json.put(PAGE_HEIGHT, ((Double) data.get("height")).intValue());
            json.put(PAGE_IGNORED, (boolean) data.get("isIgnored"));
            return json;
        } catch (Exception e) {
            Logger.e("Circler Data PageElement parser fail", e);
        }
        return null;
    }

    static JSONObject createPageElementData(View view, Page<?> viewPage) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        try {
            JSONObject json = new JSONObject();
            json.put(PAGE_PATH, viewPage.path());
            json.put(PAGE_TITLE, viewPage.getTitle());
            json.put(PAGE_LEFT, location[0]);
            json.put(PAGE_TOP, location[1]);
            json.put(PAGE_WIDTH, view.getWidth());
            json.put(PAGE_HEIGHT, view.getHeight());
            json.put(PAGE_IGNORED, false);
            return json;
        } catch (JSONException ignored) {
        }
        return null;
    }
}
