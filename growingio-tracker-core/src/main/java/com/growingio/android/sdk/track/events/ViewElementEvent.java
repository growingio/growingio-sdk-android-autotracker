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
package com.growingio.android.sdk.track.events;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.sdk.annotation.json.JsonAlias;
import com.growingio.sdk.annotation.json.JsonSerializer;

@JsonSerializer
public class ViewElementEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    private final String path;
    @Nullable
    private final String textValue;

    /**
     * keep for v3.0 sdk
     */
    @Deprecated
    @IntRange(from = 0)
    private final long pageShowTimestamp;
    private final String xpath;
    private final int index;

    /**
     * new for v4.0 sdk
     */
    @Nullable
    @JsonAlias(name = "xcontent")
    private final String xIndex;

    protected ViewElementEvent(Builder eventBuilder) {
        super(eventBuilder);
        path = eventBuilder.path;
        textValue = eventBuilder.textValue;
        pageShowTimestamp = eventBuilder.mPageShowTimestamp;
        xpath = eventBuilder.xpath;
        index = eventBuilder.index;
        xIndex = eventBuilder.xIndex;
    }

    public String getPath() {
        return checkValueSafe(path);
    }

    public String getTextValue() {
        return checkValueSafe(textValue);
    }

    public long getPageShowTimestamp() {
        return pageShowTimestamp;
    }

    public String getXpath() {
        return checkValueSafe(xpath);
    }

    public int getIndex() {
        return index;
    }

    public String getXIndex() {
        return xIndex;
    }

    public static class Builder extends BaseAttributesEvent.Builder<ViewElementEvent> {
        private String path;
        private String textValue;
        private String xpath;
        private int index = -1;

        /**
         * new for v4.0 sdk
         */
        private String xIndex;

        /**
         * keep for v3.0 sdk
         */
        @Deprecated
        private long mPageShowTimestamp;

        public Builder() {
            super(AutotrackEventType.VIEW_CLICK);
        }


        public Builder(String eventType) {
            super(eventType);
        }

        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
            return this;
        }

        public Builder setEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setTextValue(String textValue) {
            this.textValue = textValue;
            return this;
        }

        public Builder setXpath(String xpath) {
            this.xpath = xpath;
            return this;
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder setXIndex(String xIndex) {
            this.xIndex = xIndex;
            return this;
        }

        public String getPath() {
            return path;
        }

        @Override
        public ViewElementEvent build() {
            return new ViewElementEvent(this);
        }
    }
}
