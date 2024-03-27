/*
 *  Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.sdk.autotrack.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import com.growingio.android.sdk.autotrack.page.PageRule;
import com.growingio.android.sdk.track.log.Logger;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlParserUtil {
    private static final String TAG = "XmlParserUtil";
    private static final String GROWINGIO_SETTING_NODE = "growingio-setting";
    private static final String PAGE_RULE_NODE = "page-rule";
    private static final String PAGE_LIST_NODE = "page-list";
    private static final String PAGE_MATCH_NODE = "page-match";
    private static final String PAGE_NODE = "page";

    public static List<PageRule> loadPageRuleXml(Context context, int xmlResId) {
        List<PageRule> pageRules = new ArrayList<>();
        if (xmlResId == 0) return pageRules;
        Logger.d(TAG, "Start loadPageRuleXml.");
        try {
            try (XmlResourceParser xmlParser = context.getResources().getXml(xmlResId)) {
                int eventType = xmlParser.getEventType();
                String parentNode = null;
                while (eventType != XmlResourceParser.END_DOCUMENT) {
                    if (eventType == XmlResourceParser.START_TAG) {
                        if (GROWINGIO_SETTING_NODE.equals(xmlParser.getName()) && parentNode == null) {
                            parentNode = GROWINGIO_SETTING_NODE;
                        }
                        if (GROWINGIO_SETTING_NODE.equals(parentNode) && PAGE_RULE_NODE.equals(xmlParser.getName())) {
                            parentNode = PAGE_RULE_NODE;
                        }
                        if (PAGE_RULE_NODE.equals(parentNode) && PAGE_LIST_NODE.equals(xmlParser.getName())) {
                            parentNode = PAGE_LIST_NODE;
                        }

                        if (PAGE_RULE_NODE.equals(parentNode) && PAGE_MATCH_NODE.equals(xmlParser.getName())) {
                            parentNode = PAGE_MATCH_NODE;
                        }
                        if (PAGE_NODE.equals(xmlParser.getName()) && PAGE_LIST_NODE.equals(parentNode)) {
                            String pageName = xmlParser.getAttributeValue(null, "name");
                            String pageClassPath = xmlParser.getAttributeValue(null, "path");
                            pageRules.add(new PageRule(pageName, pageClassPath));
                        }

                        if (PAGE_NODE.equals(xmlParser.getName()) && PAGE_MATCH_NODE.equals(parentNode)) {
                            String pageRegex = xmlParser.getAttributeValue(null, "regex");
                            pageRules.add(new PageRule(pageRegex));
                        }
                    }
                    if (eventType == XmlResourceParser.END_TAG) {
                        if (PAGE_MATCH_NODE.equals(xmlParser.getName()) || PAGE_LIST_NODE.equals(xmlParser.getName())) {
                            parentNode = PAGE_RULE_NODE;
                        }
                    }
                    eventType = xmlParser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                Logger.e(TAG, "loadPageRuleXml failed.", e);
            }
        } catch (Resources.NotFoundException e) {
            Logger.e(TAG, "Not found xml file.", e);
        }
        return pageRules;
    }
}
