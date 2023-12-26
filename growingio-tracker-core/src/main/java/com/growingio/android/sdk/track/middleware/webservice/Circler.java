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
package com.growingio.android.sdk.track.middleware.webservice;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * start circler module.
 *
 * @author cpacm 5/10/21
 */
public class Circler {

    public final static int CIRCLE_INIT = 0;
    public final static int CIRCLE_DATA = 1;

    public final int circleDataType;
    private Map<String, String> params;

    public Circler(Map<String, String> params) {
        circleDataType = CIRCLE_INIT;
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    private CirclerData circlerData;

    public Circler(CirclerData circlerData) {
        circleDataType = CIRCLE_DATA;
        this.circlerData = circlerData;
    }

    public CirclerData getCirclerData() {
        return circlerData;
    }

    public static class CirclerData {
        private List<Map<String, Object>> elements;
        private List<Map<String, Object>> pages;
        private String screenshot;
        private double scale;
        private double width;
        private double height;

        public List<Map<String, Object>> getElements() {
            return elements;
        }

        public void setElements(List<Map<String, Object>> elements) {
            this.elements = elements;
        }

        public List<Map<String, Object>> getPages() {
            return pages;
        }

        public void setPages(List<Map<String, Object>> pages) {
            this.pages = pages;
        }

        public String getScreenshot() {
            return screenshot;
        }

        public void setScreenshot(String screenshot) {
            this.screenshot = screenshot;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            this.scale = scale;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }
}
