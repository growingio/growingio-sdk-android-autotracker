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
package com.growingio.android.flutter;

import android.util.Base64;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AttributesBuilder;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.middleware.webservice.Circler;
import com.growingio.android.sdk.track.middleware.webservice.Debugger;
import com.growingio.android.sdk.track.middleware.webservice.WebService;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Flutter 插件数据交互
 *
 * @author cpacm 2022/12/28
 */
public class FlutterPluginProvider implements TrackerLifecycleProvider {

    private static final String TAG = "FlutterPluginProvider";

    private static class SingleInstance {
        private static final FlutterPluginProvider INSTANCE = new FlutterPluginProvider();
    }

    public static FlutterPluginProvider get() {
        return SingleInstance.INSTANCE;
    }

    private FlutterPluginProvider() {
    }

    private TrackerRegistry registry;

    @Override
    public void setup(TrackerContext context) {
        registry = context.getRegistry();
    }

    @Override
    public void shutdown() {
        setFlutterMethodInterface(null);
    }

    FlutterMethodInterface flutterMethodInterface;

    public void setFlutterMethodInterface(FlutterMethodInterface flutterMethodInterface) {
        this.flutterMethodInterface = flutterMethodInterface;
        if (registry == null) return;
        // check sdk is inCircle or inDebugger
        WebService webService = registry.executeData(new Circler(Circler.CIRCLE_REFRESH), Circler.class, WebService.class);
        if (webService != null && webService.isRunning()) {
            startFlutterCircle();
            return;
        }

        webService = registry.executeData(new Debugger(Debugger.DEBUGGER_REFRESH), Debugger.class, WebService.class);
        if (webService != null && webService.isRunning()) {
            startFlutterDebugger();
        }
    }

    public void startFlutterCircle() {
        if (flutterMethodInterface != null) {
            flutterMethodInterface.startFlutterCircle();
        }
    }

    public void stopFlutterCircle() {
        if (flutterMethodInterface != null) {
            flutterMethodInterface.stopFlutterCircle();
        }
    }

    public void startFlutterDebugger() {
        if (flutterMethodInterface != null) {
            flutterMethodInterface.startFlutterDebugger();
        }
    }

    public void stopFlutterDebugger() {
        if (flutterMethodInterface != null) {
            flutterMethodInterface.stopFlutterDebugger();
        }
    }

    private String lastPagePath = null;

    public void trackFlutterPage(Map args) {
        try {
            String title = (String) args.get("title");
            String path = (String) args.get("path");
            Map<String, Object> attributes = (Map<String, Object>) args.get("attributes");
            AttributesBuilder builder = new AttributesBuilder();
            builder.addAttribute(attributes);
            long timeStamp = 0;
            if (args.containsKey("timestamp")) {
                timeStamp = (Long) args.get("timestamp");
            }
            TrackMainThread.trackMain().postEventToTrackMain(
                    new PageEvent.Builder()
                            .setPath(path)
                            .setReferralPage(lastPagePath)
                            .setTitle(title)
                            .setTimestamp(timeStamp)
                            .setAttributes(builder.build())
            );
            lastPagePath = path;
        } catch (Exception e) {
            Logger.e(TAG, e);
        }

    }

    public void trackClickEvent(Map args) {
        try {
            String eventType = (String) args.get("eventType");
            String path = (String) args.get("path");
            String xpath = (String) args.get("xpath");
            String title = (String) args.get("textValue");
            int index = (int) args.get("index");
            String xIndex = "";
            if (args.containsKey("xcontent")) {
                xIndex = (String) args.get("xcontent");
            }
            AttributesBuilder attributesBuilder = new AttributesBuilder();
            if (args.containsKey("attributes")) {
                attributesBuilder.addAttribute((Map<String, Object>) args.get("attributes"));
            }
            TrackMainThread.trackMain().postEventToTrackMain(
                    new ViewElementEvent.Builder(eventType)
                            .setPath(path)
                            .setXpath(xpath)
                            .setXIndex(xIndex)
                            .setIndex(index)
                            .setTextValue(title)
                            .setAttributes(attributesBuilder.build())
            );
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    public void trackCircleData(Map args, byte[] screenshot) {
        if (registry == null) return;
        try {
            List<Map<String, Object>> elements = (List<Map<String, Object>>) args.get("elements");
            List<Map<String, Object>> pages = (List<Map<String, Object>>) args.get("pages");
            double scale = (double) args.get("scale");
            double width = (double) args.get("width");
            double height = (double) args.get("height");
            String screenshotBase64 = null;
            if (screenshot != null) {
                screenshotBase64 = "data:image/jpeg;base64," + Base64.encodeToString(screenshot, Base64.DEFAULT);
            }
            Circler.CirclerData circlerData = new Circler.CirclerData();
            circlerData.setElements(elements);
            circlerData.setPages(pages);
            circlerData.setScreenshot(screenshotBase64);
            circlerData.setScale(scale);
            circlerData.setHeight(height);
            circlerData.setWidth(width);

            registry.loadData(new Circler(circlerData), Circler.class, WebService.class, new LoadDataFetcher.DataCallback<WebService>() {
                @Override
                public void onDataReady(WebService data) {
                    Logger.d(TAG, "send circle data success");
                }

                @Override
                public void onLoadFailed(Exception e) {
                    Logger.e(TAG, e.getMessage());
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    public void trackDebuggerData(byte[] screenshot) {
        if (registry == null) return;
        registry.loadData(new Debugger(screenshot), Debugger.class, WebService.class, new LoadDataFetcher.DataCallback<WebService>() {
            @Override
            public void onDataReady(WebService data) {
                Logger.d(TAG, "send debugger data success");
            }

            @Override
            public void onLoadFailed(Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        });
    }
}
