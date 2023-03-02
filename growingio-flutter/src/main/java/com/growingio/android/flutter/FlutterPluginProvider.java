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

package com.growingio.android.flutter;

import android.content.res.Configuration;
import android.util.Base64;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AttributesBuilder;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.webservices.Circler;
import com.growingio.android.sdk.track.webservices.Debugger;
import com.growingio.android.sdk.track.webservices.WebService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Flutter 插件数据交互
 *
 * @author cpacm 2022/12/28
 */
public class FlutterPluginProvider {

    private static final String TAG = "FlutterPluginProvider";

    private static class SingleInstance {
        private static final FlutterPluginProvider INSTANCE = new FlutterPluginProvider();
    }

    private FlutterPluginProvider() {
    }

    public static FlutterPluginProvider get() {
        return SingleInstance.INSTANCE;
    }

    FlutterMethodInterface flutterMethodInterface;

    public void setFlutterMethodInterface(FlutterMethodInterface flutterMethodInterface) {
        this.flutterMethodInterface = flutterMethodInterface;
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

    public void trackFlutterPage(Map args) {
        if (!TrackerContext.initializedSuccessfully()) return;

        try {
            String title = (String) args.get("title");
            String path = (String) args.get("path");
            Map<String, Object> attributes = (Map<String, Object>) args.get("attributes");
            AttributesBuilder builder = new AttributesBuilder();
            if (attributes != null && attributes.keySet() != null) {
                for (String key : attributes.keySet()) {
                    Object value = attributes.get(key);
                    if (value instanceof List) {
                        builder.addAttribute(key, (List) value);
                    } else if (value instanceof String[]) {
                        builder.addAttribute(key, (String[]) value);
                    } else if (value instanceof Set) {
                        builder.addAttribute(key, (Set) value);
                    } else {
                        builder.addAttribute(key, String.valueOf(value));
                    }
                }
            }
            long timeStamp = (Long) args.get("timestamp");
            String orientation = TrackerContext.get().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                    ? PageEvent.ORIENTATION_PORTRAIT : PageEvent.ORIENTATION_LANDSCAPE;

            TrackMainThread.trackMain().postEventToTrackMain(
                    new PageEvent.Builder()
                            .setPath(path)
                            .setTitle(title)
                            .setTimestamp(timeStamp)
                            .setOrientation(orientation)
                            .setAttributes(builder.build())
            );
        } catch (Exception e) {
            Logger.e(TAG, e);
        }

    }

    public void trackClickEvent(Map args) {
        if (!TrackerContext.initializedSuccessfully()) return;

        try {
            String eventType = (String) args.get("eventType");
            String path = (String) args.get("path");
            String xpath = (String) args.get("xpath");
            long timeStamp = (Long) args.get("pageShowTimestamp");
            String title = (String) args.get("textValue");
            int index = (int) args.get("index");

            TrackMainThread.trackMain().postEventToTrackMain(
                    new ViewElementEvent.Builder(eventType)
                            .setPath(path)
                            .setPageShowTimestamp(timeStamp)
                            .setXpath(xpath)
                            .setIndex(index)
                            .setTextValue(title)
            );
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    public void trackCircleData(Map args, byte[] screenshot) {
        if (!TrackerContext.initializedSuccessfully()) return;
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


            TrackerContext.get().loadData(new Circler(circlerData), Circler.class, WebService.class, new LoadDataFetcher.DataCallback<WebService>() {
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
        TrackerContext.get().loadData(new Debugger(screenshot), Debugger.class, WebService.class, new LoadDataFetcher.DataCallback<WebService>() {
            @Override
            public void onDataReady(WebService data) {
                Logger.d(TAG, "send circle data success");
            }

            @Override
            public void onLoadFailed(Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        });
    }
}
